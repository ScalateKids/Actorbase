/**
  * The MIT License (MIT)
  * <p/>
  * Copyright (c) 2016 ScalateKids
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * <p/>
  * @author Scalatekids
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.actorsystem.actors.storekeeper

import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props }
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.cluster.pubsub.DistributedPubSub
import akka.actor.SupervisorStrategy._

import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp

import com.actorbase.actorsystem.messages.StorekeeperMessages._
import com.actorbase.actorsystem.messages.StorefinderMessages.{PartialMapTransaction, UpdateCollectionSize}
import com.actorbase.actorsystem.messages.WarehousemanMessages.{ Init, Save }
import com.actorbase.actorsystem.messages.ClientActorMessages.Response
import com.actorbase.actorsystem.messages.ManagerMessages.OneMore
import com.actorbase.actorsystem.actors.warehouseman.Warehouseman
import com.actorbase.actorsystem.utils.CryptoUtils

import scala.concurrent.duration._
import scala.language.postfixOps

object Storekeeper {

  def props(n: String, o: String, s: Int): Props = Props(classOf[Storekeeper], n, o, s)

}

/**
  *
  * @param data
  * @param manager
  * @param range
  * @param maxSize
  */
class Storekeeper(private val collectionName: String, private val collectionOwner: String, indicativeSize: Int) extends Actor with ActorLogging {

  val mediator = DistributedPubSub(context.system).mediator
  // subscribe to the topic named "persist-data"
  mediator ! Subscribe("persist-data", self)

  private val warehouseman = context.actorOf(Warehouseman.props( collectionOwner + collectionName ))
  private var manager: Option[ActorRef] = None
  private var checked = false
  val cluster = Cluster(context.system)

  warehouseman ! Init( collectionName, collectionOwner)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception => Resume
    }

  /**
    * Receive method of the Storekeper actor, it does different things based on the message it receives:<br>
    * _InitMn: when the actor receives this message it initialize its corresponding manager actor.<br>
    * _GetItem: when the actor receives this message it returns a value associated with the input key<br>
    * _GetAll: when the actor receives this message it returns all its contents<br>
    * _RemoveItem: when the actor receives this message it remove a value associated with the input key</br>
    * _ins: when the actor receives this message it insert an item to the collection; it can or not allow the update of the item depending on what is specified by the boolean param update</br>
    * _Persist: when the actor receives this message it persists its data to disk.<br>
    *
    */
  def receive = running(Map[String, Array[Byte]]().empty)

  /**
    * Represents the state of the actor during process of the messages inside
    * his mailbox
    */
  def running(data: Map[String, Array[Byte]]): Receive = {

    case message: StorekeeperMessage => message match {

      /**
        * InitMn message, this actor will initialize its corresponding manager actor
        *
        * @param mn a manager actor rapresenting the corresponding manager
        */
      case InitMn(mn) => manager = Some(mn)

      /**
        * GetItem message, this actor will send back a value associated with the input key
        *
        * @param key a String representing the key of the item to be returned (sta roba sarà da cambiare)
        *
        */
      case GetItem(key, uuid)  =>
        println("SK "+data.contains(key)+" of collection "+collectionName+"With data \n"+data)
        data get key map (v => sender ! Right(Response(CryptoUtils.bytesToAny(v)))) getOrElse sender ! Left("UndefinedKey")

      /**
        * GetAllItem message, this actor will send back the collection name and all the collection.
        */
      case GetAll(parent, requester) =>
        if (data.nonEmpty)
          parent ! PartialMapTransaction(requester, sender, data)

      /**
        * RemoveItem message, when the actor receive this message it will erase the item associated with the
        * key in input. This method doesn't throw an exception if the item is not present.
        */
      case RemoveItem(parent, key) =>
        if (data contains(key)) {
          parent ! UpdateCollectionSize(0, false)
          sender ! "OK"
          context become running(data - key)
        } else sender ! "UndefinedKey"

      /**
        * Insert message, insert a key/value into a designed collection
        *
        * @param key a String representing the new key to be inserted
        * @param value a Any object type representing the value to be inserted
        * with associated key, default to Array[Byte] type
        * @param update a Boolean flag, define the insert behavior (with or without
        * updating the value)
        *
        */
      case ins: InsertItem =>
        /**
          * private method that insert an item to the collection, can allow the update of the item or not
          * changing the param update
          *
          * @param update boolean. 1 if the insert allow an update, 0 otherwise
          * @param key String representing the key of the item
          */
        def insertOrUpdate(update: Boolean, key: String): Boolean = {
          var done = true
          if (!update && !data.contains(key)) {
            insertWithoutUpdate
          }
          else if (!update && data.contains(key)) {
            done = false
          }
          else if (update && !data.contains(key)){
            insertWithoutUpdate
          }
          done
        }

        /**
          * Private method used to insert an item without overwriting. This method update the size
          * of the collection and proceed to ask the manager to create another Storekeeper if
          * this is full
          */
        def insertWithoutUpdate: Unit = {
          // log.info("SK: Got work!")
          val w = ins.value.length.toLong + ins.key.getBytes("UTF-8").length.toLong
          ins.parentRef ! UpdateCollectionSize(w, true)
          // if (data.size > indicativeSize && !checked) {
          //   checked = true
          //   manager map (_ ! OneMore) getOrElse (checked = false)
          // }
        }

        if (insertOrUpdate(ins.update, ins.key) == true) {
          sender ! "OK"
          context become running(data + (ins.key -> ins.value))
        } else sender ! "DuplicatedKey"

      /**
        * Persist data to disk
        */
      case Persist => if (data.size > 0 && collectionOwner != "anonymous") warehouseman ! Save( data )

    }
  }
}

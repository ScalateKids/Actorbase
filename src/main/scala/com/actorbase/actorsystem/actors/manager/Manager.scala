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

package com.actorbase.actorsystem.actors.manager

import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props }
import akka.routing.{ ActorRefRoutee, AddRoutee, AdjustPoolSize, GetRoutees }
import akka.actor.SupervisorStrategy._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
// import akka.cluster.routing.ClusterRouterPool
// import akka.cluster.routing.ClusterRouterPoolSettings
// import akka.routing.ConsistentHashingPool

import com.actorbase.actorsystem.actors.storekeeper.Storekeeper
import com.actorbase.actorsystem.messages.StorekeeperMessages.InitMn
import com.actorbase.actorsystem.messages.ManagerMessages._
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object Manager {

  def props(collection: String, owner: String, router: ActorRef): Props = Props(classOf[Manager], collection, owner, router)

}

/**
  * This class represent the Manager actor, it tries to maintain the equilibrium inside the SKs by tracing the number of
  * entries stored in each map. If some SK actor is under heavy load, (number of
  * entries is beyond a given threshold) they will create new actor of type SK
  * to properly redistribute that load and add it to the SF router.
  */
class Manager(val collection: String, val owner: String, val router: ActorRef) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5 seconds)
  var reports = 0
  val config = ConfigFactory.load().getConfig("storekeepers")
  // val instancePerNode = config getInt "instance-per-node"

  /**
    * Method that overrides the supervisorStrategy method.
    */

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception => Resume
    }
  /**
    * Receive method of the Manager actor it inserts the item in the collection requested by the user.<br>
    * _OneMore: when the actor receives this message it creates a new storekeeper <br>
    *
    */
  def receive = { case message: ManagerMessage =>

    message match {

      case OneMore =>
        reports += 1
        // log.info("new storekeeper added to [POOL]")
        // val newSk = context.actorOf(ClusterRouterPool(ConsistentHashingPool(0),
        // ClusterRouterPoolSettings(config getInt "max-instances", 1, true, useRole = None)).props(Storekeeper.props(collection, owner, config getInt "size")), s"managerStorekeeper-$reports")
        // val newSk = context.actorOf(Storekeeper.props(collection, owner, config getInt "size"), s"managerStorekeeper-$reports")
        // newSk ! InitMn(self)
        // router ! AddRoutee(ActorRefRoutee(newSk))
        // val future = (router ? GetRoutees).mapTo
        // log.info()
        router ! AdjustPoolSize(30 + reports)
    }
  }
}

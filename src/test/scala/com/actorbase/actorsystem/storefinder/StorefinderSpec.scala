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
  *
  * @author Scalatekids TODO DA CAMBIARE
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.actorsystem.storefinder

import akka.util.Timeout
import com.actorbase.actorsystem.actors.authactor.AuthActor
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestActorRef, ImplicitSender, TestProbe}

import com.actorbase.actorsystem.ActorSystemSpecs.ActorSystemUnitSpec
import com.actorbase.actorsystem.utils.ActorbaseCollection
import com.actorbase.actorsystem.actors.storefinder.Storefinder
import com.actorbase.actorsystem.messages.StorefinderMessages._

class StorefinderSpec extends TestKit(ActorSystem("StorefinderSpec",
  ConfigFactory.parseString("""
akka.remote.netty.tcp.port = 0,
akka.actor.provider = "akka.cluster.ClusterActorRefProvider",
akka.loglevel = "OFF"
  """))) with ActorSystemUnitSpec with ImplicitSender {

  implicit val timeout = Timeout(25 seconds)

  /**
    * afterAll method, triggered after all test have ended, it shutdown the
    * actorsystem.
    */
  override def afterAll() : Unit = system.shutdown

  "Storefinder Actor" should {

    val actbColl = new ActorbaseCollection("testCollection", "anonymous")
    val authRef = TestActorRef(new AuthActor)
    val sfRef = TestActorRef(new Storefinder( actbColl, authRef ))
    val p = TestProbe()

    "be created" in{
      assert(sfRef != None)
    }

    "insert and get an item" in {
      val value = "value".getBytes("UTF-8")
      p.send( sfRef, Insert("key", value , false) )
      p.send( sfRef, Get("key") )
      p.expectMsg("OK")
    }

    "get all items" in {  // response is null, can't expect anything
      p.send( sfRef, GetAllItems )
    }

    "remove an item" in {
      val value = "value".getBytes("UTF-8")
      p.send( sfRef, Remove("key"))
      p.expectMsg("OK")
    }

    "update the collection size" in { // response is null, can't expect anything
      p.send( sfRef, UpdateCollectionSize( 10l, true ) )
    }

    "receive the message PartialMapTransaction" in {  // response is null, can't expect anything
      p.send( sfRef, PartialMapTransaction( "anonymous", sfRef, Map[String, Array[Byte]]("key" -> "value".getBytes ) ) )
    }
  }

}

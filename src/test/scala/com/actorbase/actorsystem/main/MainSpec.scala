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

package com.actorbase.actorsystem.main

import akka.util.Timeout
import com.actorbase.actorsystem.utils.ActorbaseCollection
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestActorRef, ImplicitSender, TestProbe}

import com.actorbase.actorsystem.ActorSystemSpecs._
import com.actorbase.actorsystem.actors.main.Main
import com.actorbase.actorsystem.actors.authactor.AuthActor
import com.actorbase.actorsystem.utils.ActorbaseCollection._
import com.actorbase.actorsystem.messages.MainMessages._
import com.actorbase.actorsystem.messages.AuthActorMessages.{AddCredentials}

class MainSpec extends TestKit(ActorSystem("MainSpec",
  ConfigFactory.parseString("""
akka.remote.netty.tcp.port = 0,
akka.actor.provider = "akka.cluster.ClusterActorRefProvider",
akka.loglevel = "OFF"
"""))) with ActorSystemUnitSpec with ImplicitSender {

  implicit val timeout = Timeout(5 seconds)

  /**
    * afterAll method, triggered after all test have ended, it shutdown the
    * actorsystem.
    */
  override def afterAll() : Unit = system.shutdown

  "Main actor" should{

    val p = TestProbe()
    val authProxy = TestActorRef( new AuthActor)
    val mainActorRef = TestActorRef( new Main(authProxy) )
    val testColl = new ActorbaseCollection("testCollection", "anonymous")

    "should be created" in {
      assert(mainActorRef != None)
    }

    "create a new collection" in {
      p.send(mainActorRef, CreateCollection("admin", testColl))
      p.expectMsg("OK")
    }

    "insert and retrieve an item" in {
      val value = "testValue".getBytes("UTF-8")
      p.send( mainActorRef, InsertTo("anonymous", testColl, "testKey",  value, false))
      p.send( mainActorRef, GetFrom("anonymous", testColl, "testKey"))
      p.expectMsg("OK")
    }

    "remove an item" in {
      val value = "testValue".getBytes("UTF-8")
      p.send(mainActorRef, RemoveFrom("anonymous", testColl.getUUID, "testKey"))
      p.expectMsg("OK")
    }

    "add a contributor to a collection" in {
      p.send( authProxy, AddCredentials("pluto", "p4sswordPluto"))
      p.send( mainActorRef, AddContributor("anonymous", "pluto", ReadWrite, testColl.getUUID))
      p.expectMsg("OK") //sent by AddCredentials
      p.expectMsg("OK") //sent by AddContributor
    }

    "return an error message trying to add a contributor to a collection that does not exists" in {
      p.send( mainActorRef, AddContributor("anonymous", "pluto", ReadWrite, "notExistingUUID"))
      p.expectMsg("UndefinedCollection")
    }

    "return an error message trying to add a contributor that does not exists to a collection" in {
      p.send( mainActorRef, AddContributor("anonymous", "notExistingUsername", ReadWrite, testColl.getUUID))
      p.expectMsg("UndefinedUsername")
    }

    "remove a contributor from a collection" in {
      p.send( mainActorRef, RemoveContributor("anonymous", "pluto", testColl.getUUID ))
      p.expectMsg("OK")
    }

    "return an error message trying to remove a contributor to a collection that does not exists" in {
      p.send( mainActorRef, RemoveContributor("anonymous", "pluto", "notExistingUUID"))
      p.expectMsg("UndefinedCollection")
    }

    "receive the message CompleteTransaction" in {
      p.send( mainActorRef, CompleteTransaction( "anonymous", authProxy, testColl, Map[String, Array[Byte]]("key" -> "value".getBytes("UTF-8") ) ) )
    }

    "remove a collection" in {
      p.send( mainActorRef, RemoveFrom("anonymous", testColl.getUUID) )
      p.expectMsg("OK")
    }

    "create a collection that does not exists in the system just by adding an item to it" in {
      val notExistingCollection = new ActorbaseCollection("collectionName", "anonymous")
      p.send( mainActorRef, InsertTo("anonymous", notExistingCollection, "anotherKey",  "value".getBytes("UTF-8"), false))
      p.expectMsg("OK")
    }

    "return an error message trying to insert an item with a key already " +
    "existing in the system without overwriting" in {
      val notExistingCollection = new ActorbaseCollection("collectionName", "anonymous")
      p.send( mainActorRef, InsertTo("anonymous", notExistingCollection, "anotherKey", "value".getBytes("UTF-8"), false))
      p.expectMsg("DuplicatedKey")
    }

    "return an error message trying to delete a collection that does not exists" in {
      p.send( mainActorRef, RemoveFrom("anonymous", "notExistingUUID"))
      p.expectMsg("UndefinedCollection")
    }
  }
}

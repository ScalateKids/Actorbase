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
import com.actorbase.actorsystem.actors.storefinder.Storefinder
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestActorRef, ImplicitSender, TestProbe}

import com.actorbase.actorsystem.ActorSystemSpecs.ActorSystemUnitSpec
import com.actorbase.actorsystem.utils.ActorbaseCollection
import com.actorbase.actorsystem.actors.storekeeper.Storekeeper
import com.actorbase.actorsystem.messages.StorekeeperMessages._
import com.actorbase.actorsystem.messages.StorefinderMessages.PartialMapTransaction

class StorekeeperSpec extends TestKit(ActorSystem("StorekeeperSpec",
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

  val valore = "value".getBytes("UTF-8")

  "Storekeeper Actor" should{

    val collName = "testName"
    val collOwner = "testOwner"
    val saveMethod = "snapshot"
    val skRef = TestActorRef(new Storekeeper( collName, collOwner, saveMethod, 256 ))
    val actbColl = new ActorbaseCollection( collName, collOwner)
    val authRef = TestActorRef(new AuthActor)
    val sfRef = TestActorRef(new Storefinder( actbColl, authRef ))
    val p = TestProbe()

    val valore = "value".getBytes("UTF-8")

    "be created" in {
      assert(skRef != None)
    }

    "insert an item" in {
      p.send( skRef, InsertItem(sfRef, "key", valore , false) )
      p.expectMsg("OK")
    }

    "remove an item" in {
      p.send( skRef, RemoveItem(sfRef, "key") )
      p.expectMsg("OK")
    }

    "send an error message if the key to be removed isn't in this storekeeper" in { // no response, can't expect anything if uncommented makes remove stop working
      p.send( skRef, RemoveItem(sfRef, "key") )
      p.expectMsg("UndefinedKey")
    }

    "send an error message if the key searched isn't in this storekeeper" in { // no response, can't expect anything if uncommented makes remove stop working
      p.send( skRef, GetItem("key") )
      p.expectMsg(Left("UndefinedKey"))
    }

    "get an item" in { // no response, can't expect anything if uncommented makes remove stop working
      p.send( skRef, InsertItem(sfRef, "key", valore , false) )
      p.send( skRef, GetItem("key") )
      p.expectMsg("OK")
    }

    "return all items" in {
      p.send( skRef, InsertItem(sfRef, "key", valore , false) )
      p.send( skRef, GetAll( p.ref, "anonymous" ) )
      p.expectMsg("OK")
    }

    "persist data sending message to the warehouseman" in {
      p.send( skRef, Persist )
      p.expectMsg( PartialMapTransaction( "anonymous", p.ref, Map[String, Array[Byte]]("key" -> valore)) )
    }

    "able to receive a message Initmn to initialize his manager" in {   // no response, can't expect anything
      p.send( skRef, InitMn( sfRef))

    }
  }

}

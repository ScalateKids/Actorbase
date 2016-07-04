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

package com.actorbase.actorsystem.manager

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestActorRef, ImplicitSender, TestProbe}

import com.actorbase.actorsystem.ActorSystemSpecs.ActorSystemUnitSpec
import com.actorbase.actorsystem.actors.manager.Manager
import com.actorbase.actorsystem.messages.ManagerMessages.OneMore
import com.actorbase.actorsystem.actors.storekeeper.Storekeeper

class ManagerSpec extends TestKit(ActorSystem("ManagerSpec",
  ConfigFactory.parseString("""
akka.remote.netty.tcp.port = 0,
akka.actor.provider = "akka.cluster.ClusterActorRefProvider",
akka.loglevel = "OFF"
  """))) with ActorSystemUnitSpec with ImplicitSender {

  /**
    * afterAll method, triggered after all test have ended, it shutdown the
    * actorsystem.
    */
  override def afterAll() : Unit = system.shutdown


  "Manager Actor" should{

    val collName = "testCollection"
    val collOwner = "testOwner"
    val skRef = TestActorRef(new Storekeeper( collName, collOwner, 10 ))
    val mnRef = TestActorRef(new Manager( collName, collOwner, skRef ))

    val p = TestProbe()

    "be created" in{
      assert(mnRef != None)
    }

    "create one storekeeper" in {
      p.send( mnRef, OneMore )  // no response, can't expect any message
    }
  }
}

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

package com.actorbase.actorsystem.clientactor

import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestActorRef, ImplicitSender, TestProbe}

import com.actorbase.actorsystem.ActorSystemSpecs._
import com.actorbase.actorsystem.messages.ClientActorMessages._
import com.actorbase.actorsystem.actors.main.Main
import com.actorbase.actorsystem.actors.authactor.AuthActor
import com.actorbase.actorsystem.actors.clientactor.ClientActor

class ClientActorSpec extends TestKit(ActorSystem("ClientActorSpec",
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

  "ClientActor" should {

    val authProxy = TestActorRef[AuthActor]
    val mainActorRef = TestActorRef( new Main(authProxy) )
    val clientActorRef = TestActorRef( new ClientActor(mainActorRef, authProxy))
    val p = TestProbe()

    val ab = "value".getBytes("UTF-8")

    "be created" in {
      assert(clientActorRef != None)
    }

    "receive the message Response" in {
      p.send( clientActorRef, Response( ab ) )
    }

    "receive the message MapResponse" in {
      p.send( clientActorRef, MapResponse("user", "user", Map[String, Boolean]("admin" -> true), Map[String, Any]("key" -> "value")) )
    }

    "receive the message ListResponse" in {
      p.send( clientActorRef, ListResponse(List[String]("item1", "item2", "item3") ) )
    }
  }
}

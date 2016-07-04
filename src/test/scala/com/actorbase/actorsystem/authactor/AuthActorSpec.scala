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

package com.actorbase.actorsystem.authactor

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestActorRef, ImplicitSender, TestProbe}

import com.typesafe.config.ConfigFactory

import com.actorbase.actorsystem.utils.ActorbaseCollection
import com.actorbase.actorsystem.actors.authactor.AuthActor
import com.actorbase.actorsystem.messages.AuthActorMessages._
import com.actorbase.actorsystem.ActorSystemSpecs.ActorSystemUnitSpec


/**
  * Userkeeper specification tests
  *
  * @param
  * @return
  * @throws
  */
class AuthActorSpec extends TestKit(ActorSystem("AuthActorSpec",
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

  "AuthActor" should {

    val authRef = TestActorRef[AuthActor]
    val actbColl = new ActorbaseCollection("testOwner", "testName")
    val p = TestProbe()

    "be created" in {
      assert(authRef != None)
    }

    "receive the message AddCredential and add an user to the system" in {
      p.send(authRef, AddCredentials("pippo", "Pluto7632"))
      p.expectMsg("OK")
    }

    "Return an error message if the user that has to be created has a username already used in the system" in {
      p.send( authRef, AddCredentials("pippo", "Pluto74632"))
      p.expectMsg("UsernameAlreadyExists")
    }

    "receive the message Authenticate and check if the credentials are valid" in {
      p.send(authRef, Authenticate("pippo", "Pluto7632"))
      p.expectMsg(Some("pippo", "Pluto7632"))
    }

    "receive the message AddCollectionTo and adding a collection to a user" in {
      p.send(authRef, AddCollectionTo("pippo", actbColl, ActorbaseCollection.Read))
      p.expectMsg("OK")
    }

    "receive the message RemoveCollectionFrom and removing a collection from a user" in {
      p.send(authRef, RemoveCollectionFrom("pippo", actbColl))
      p.expectMsg("OK")
    }

    "receive the message UpdateCredential and change the password of a user" in {
      import akka.actor.SupervisorStrategy._
      p.send(authRef, UpdateCredentials("pippo", "Pluto7632", "Pluto7633"))
      p.expectMsg("OK")
      p.expectMsg(Stop)
    }

    "Receive the message RemoveCredential and remove a user from the system" in {
      p.send( authRef, RemoveCredentials("pippo"))
      p.expectMsg("OK")
    }

    "Receive the message ResetPassword and reset a user password to the Actorbase default" in {
      p.send( authRef, ResetPassword("pippo"))
      p.expectMsg("OK")
    }

    "Return an error message if the username to remove is not existing in the system" in {
      p.send( authRef, RemoveCredentials("notExistingUsername"))
      p.expectMsg("UndefinedUsername")
    }

    "Return an error message if the credentials passed to log in the system are not valid" in {
      p.send( authRef, Authenticate("userNotExisting", "p4sswordOfUser"))
      p.expectMsg(Some("None", "None"))
    }


    "Return an error message if the username combined to the password that has to be changed is not existing in the system" in {
      p.send( authRef, UpdateCredentials("notExistingUsername", "P4ssword", "NewP4ssword"))
      p.expectMsg("UndefinedUsername")
    }
  }

}

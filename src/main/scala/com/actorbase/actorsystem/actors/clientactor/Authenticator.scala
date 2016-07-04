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

package com.actorbase.actorsystem.actors.clientactor

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import spray.routing.authentication.BasicAuth
import spray.routing.authentication.UserPass
import spray.routing.directives.AuthMagnet

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

import com.actorbase.actorsystem.messages.AuthActorMessages.Authenticate

/**
  * Authenticator, mix it with a HttpServiceBase class to give basic
  * authentication capabilities
  *
  */
trait Authenticator {

  implicit val timeout = Timeout(5 seconds)

  var login: Option[(String, String)] = Some("anonymous", "Actorb4se")

  /**
    * Basic authentication method
    *
    * @param ec ExecutionContext
    * @param authProxy ActorRef representing a reference to the Authenticator actor
    * @return a BasicAuth uncrypted for a private area
    */
  def basicUserAuthenticator(implicit ec: ExecutionContext, authProxy: ActorRef): AuthMagnet[(String, String)] = {

    /**
      * Authentication method, call for validateUser and test for a matching
      * password against the one saved into the system
      *
      * @param userPass Option[UserPass] extract by the method authenticate
      * @return a reference to a Future of type AuthInfo containing the
      * credentials of the authenticated user
      */
    def authenticator(userPass: Option[UserPass]): Future[Option[(String, String)]] = {
      if (login.exists(l => (l._1 == userPass.get.user) && (l._2 == userPass.get.pass))) Future { login }
      else {
        val log = (authProxy ? Authenticate(userPass.get.user, userPass.get.pass)).mapTo[Option[(String, String)]]
        log onSuccess {
          case ok => login = ok
        }
        log
      }
    }

    BasicAuth(authenticator _, realm = "Private area")
  }
}

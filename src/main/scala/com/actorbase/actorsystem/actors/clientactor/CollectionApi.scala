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
import akka.util.Timeout
import akka.pattern.ask
import spray.http.{ HttpResponse, StatusCodes }
import spray.httpx.SprayJsonSupport._
import spray.httpx.marshalling.ToResponseMarshallable
import spray.httpx.marshalling._
import spray.httpx.encoding._
import spray.routing._
import spray.routing.HttpService

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import java.util.Base64

import com.actorbase.actorsystem.utils.ActorbaseCollection
import com.actorbase.actorsystem.messages.MainMessages.{InsertTo, GetFrom, RemoveFrom, CreateCollection, AddContributor, RemoveContributor}
import com.actorbase.actorsystem.messages.ClientActorMessages._
import com.actorbase.actorsystem.messages.AuthActorMessages.{ ListCollectionsOf }
import com.actorbase.actorsystem.utils.CryptoUtils

/**
  * Trait used to handle routes that are related to ActorbaseCollection
  */
trait CollectionApi extends HttpServiceBase with Authenticator {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def base64ToBytes(in: String): Array[Byte] = {
    Base64.getUrlDecoder.decode(in)
  }

  def base64ToString(in: String): String = {
    new String(Base64.getUrlDecoder.decode(in), "UTF-8")
  }
  // val simpleCache = routeCache(maxCapacity = 1000, timeToIdle = Duration("30 min"))

  /**
    * HTTP routes mapped to handle CRUD operations, these should be nouns
    * (not verbs!) e.g.
    *
    * GET /collections                       - List all collections
    * GET /collections/customers             - Retrieve collection customers
    * POST /collections/customers            - Insert new customer
    * PUT /collections/customers/aracing     - Update
    * DELETE /collections/customers/aracing  - Delete key aracing from customers
    *
    * @param main: an ActorRef pointing to a main actor
    * @param authProxy an ActorRef pointing to a AuthActor actor
    ° @return a spray Route type object
    *
    */
  def collectionsDirectives(main: ActorRef, authProxy: ActorRef): Route = {

    implicit val timeout = Timeout(90 seconds)
    /**
      * Collections route, manage all collection related operations, based
      * on the request received in the form of:
      *
      * GET collections/<collection>/<key>
      * Retrieve value associated to <key> from the collection <collection>
      *
      * POST collections/<collection>/<key>
      * Insert a value associated to <key> into the collection <collection>
      * contained inside a binary payload
      *
      * PUT collections/<collection>/<key>
      * Update a value associated to <key> into the collection <collection>
      * contained inside a binary payload
      *
      * DELETE collections/<collection>/<key>
      * remove an item of key <key> from the collection <collection>
      *
      * All routes return a ToResponseMarshallable
      */
    pathPrefix("collections") {
      pathEndOrSingleSlash {
        authenticate(basicUserAuthenticator(ec, authProxy)) { authInfo =>
          get {
            complete {
              (authProxy ? ListCollectionsOf(authInfo._1)).mapTo[ListTupleResponse]
            }
          }
        }
      }
    } ~
    pathPrefix("collections" / "\\S+".r) { collection =>
      pathEndOrSingleSlash {
        authenticate(basicUserAuthenticator(ec, authProxy)) { authInfo =>
          get {
            headerValueByName("owner") { owner =>
              complete {
                val coll = ActorbaseCollection(collection, base64ToString(owner))
                  (main ? GetFrom(authInfo._1, coll))
                  .mapTo[Either[String, MapResponse]]
                  .map { result =>
                    result match {
                      case Left(string) => HttpResponse(StatusCodes.NotFound, entity = string): ToResponseMarshallable
                      case Right(map) => map: ToResponseMarshallable
                    }
                  }
              }
            }
          } ~
          post {
            headerValueByName("owner") { owner =>
              complete {
                val coll = ActorbaseCollection(collection, base64ToString(owner))
                  (main ? CreateCollection(authInfo._1, coll)).mapTo[String]
              }
            }
          } ~
          delete {
            headerValueByName("owner") { owner =>
              complete {
                (main ? RemoveFrom(authInfo._1, base64ToString(owner) + collection)).mapTo[String]
              }
            }
          }
        }
      } ~
      pathSuffix("\\S+".r) { key =>
        pathEndOrSingleSlash {
          authenticate(basicUserAuthenticator(ec, authProxy)) { authInfo =>
            headerValueByName("owner") { owner =>
              get {
                complete {
                  val coll = ActorbaseCollection(collection, base64ToString(owner))
                    (main ? GetFrom(authInfo._1, coll, key))
                    .mapTo[Either[String, Response]]
                    .map { result =>
                      result match {
                        case Left(string) => HttpResponse(StatusCodes.NotFound, entity = string): ToResponseMarshallable
                        case Right(response) => response: ToResponseMarshallable
                      }
                    }
                }
              }
            } ~
            delete {
              headerValueByName("owner") { owner =>
                complete {
                  (main ? RemoveFrom(authInfo._1, base64ToString(owner) + collection, key)).mapTo[String]
                }
              }
            } ~
            post {
              decompressRequest() {
                headerValueByName("owner") { owner =>
                  entity(as[String]) { value =>
                    detach() {
                      complete {
                        val coll = ActorbaseCollection(collection, base64ToString(owner))
                          (main ? InsertTo(authInfo._1, coll, key, base64ToBytes(value))).mapTo[String]
                      }
                    }
                  }
                }
              }
            } ~
            put {
              decompressRequest() {
                headerValueByName("owner") { owner =>
                  entity(as[String]) { value =>
                    detach() {
                      complete {
                        val coll = ActorbaseCollection(collection, base64ToString(owner))
                          (main ? InsertTo(authInfo._1, coll, key, base64ToBytes(value), true)).mapTo[String]
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  } ~
  pathPrefix("contributors" / "\\S+".r) { collection =>
    pathEndOrSingleSlash {
      authenticate(basicUserAuthenticator(ec, authProxy)) { authInfo =>
        // get {
        //   complete("contr")
        // } ~
        post {
          decompressRequest() {
            headerValueByName("permission") { permission =>
              headerValueByName("owner") { owner =>
                entity(as[String]) { value =>
                  detach() {
                    complete {
                      val user = new String(base64ToBytes(value), "UTF-8")
                      val originalOwner = base64ToString(owner)
                      val uuid = originalOwner + collection
                      if (base64ToString(permission) == "read")
                        (main ? AddContributor(authInfo._1, user, ActorbaseCollection.Read, uuid)).mapTo[String]
                      else (main ? AddContributor(authInfo._1, user, ActorbaseCollection.ReadWrite, uuid)).mapTo[String]
                    }
                  }
                }
              }
            }
          }
        } ~
        delete {
          decompressRequest() {
            headerValueByName("owner") { owner =>
              entity(as[String]) { value =>
                detach() {
                  complete {
                    val user = new String(base64ToBytes(value), "UTF-8")
                    val uuid = base64ToString(owner) + collection
                      (main ? RemoveContributor(authInfo._1, user, uuid)).mapTo[String]
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

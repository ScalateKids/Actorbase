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

package com.actorbase.actorsystem.actors.authactor

import akka.actor.{ Actor, ActorLogging, Cancellable, OneForOneStrategy }
import akka.actor.SupervisorStrategy._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish

import com.actorbase.actorsystem.messages.StorekeeperMessages.Persist
import com.actorbase.actorsystem.messages.AuthActorMessages._
import com.actorbase.actorsystem.messages.ClientActorMessages.{ ListResponse, ListTupleResponse }
import com.actorbase.actorsystem.utils.{ ActorbaseCollection, CryptoUtils }
import com.github.t3hnar.bcrypt._
import com.typesafe.config.ConfigFactory
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import java.io.File

/**
  * Class that represents an AuthActor. This actor is a singleton cluster actor used to
  * store the users profiles data. It's also used to check the credential on the login attempts.
  * This actor is responsible to persist the users profile datas on the filesystem
  */
class AuthActor extends Actor with ActorLogging {

  private val config = ConfigFactory.load().getConfig("persistence")
  // the rootfolder in which to store the users data
  private val rootFolder = config.getString("save-folder") + "usersdata/"

  private val initDelay = 20 seconds       // delay for the first persistence message to be sent
  private val intervalDelay = 50 seconds   // interval in-between each persistence message has to be sent
  private var scheduler: Cancellable = _   // akka scheduler used to track time
                                           // activate the extension
  val mediator = DistributedPubSub(context.system).mediator

  /**
    *  Override of the preStart Actor method
    */
  override def preStart = {
    scheduler = context.system.scheduler.schedule(
      initialDelay = initDelay,
      interval = intervalDelay,
      receiver = self,
      message = PersistDB
    )
    //   persist(Set[Profile](Profile("admin", "Actorb4se", Set.empty[ActorbaseCollection])))
  }

  /**
    * Override of the supervisionStrategy Actor method
    */
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception => Resume
    }

  /**
    * Override of the receive Actor method. Set the actor in the running state
    * with a default Set of Profiles, letting him receive a variety of messages
    * explained in the running method scaladoc
    */
  /*override def receive = running(Set[Profile](Profile("admin", "Actorb4se".bcrypt(generateSalt), Set.empty[ActorbaseCollection]),
    Profile("anonymous", "Actorb4se".bcrypt(generateSalt), Set.empty[ActorbaseCollection])))*/
    override def receive = running(Set[Profile]())

  /**
    * Method used to persist the users data to filesystem
    *
    * @param profiles: a Set of Profile containing all the profiles of the users to persist on filesystem
    * @return no return value
    */
  def persist(profiles: Set[Profile]): Unit = {
    var profileMap = Map.empty[String, String]
    var contributorMap = Map.empty[String, List[(String, Boolean)]]
    profiles map  { x =>
      profileMap += (x.username -> x.password)
      var list = List.empty[(String, Boolean)]
      x.getCollections filter (c => c.containsReadContributor(x.username)) map { rc =>
        list ::= (rc.getUUID -> false)
      }
      x.getCollections filter (c => c.containsReadWriteContributor(x.username)) map { rc =>
        list ::= (rc.getUUID -> true)
      }
      contributorMap += (x.username -> list)
    }
    val key = config getString("encryption-key")
    val encryptedProfilesFile = new File(rootFolder + "/usersdata.shadow")
    encryptedProfilesFile.getParentFile.mkdirs
    val encryptedContributorsFile = new File(rootFolder + "/contributors.shadow")
    encryptedContributorsFile.getParentFile.mkdirs
    CryptoUtils.encrypt(key, profileMap, encryptedProfilesFile)
    CryptoUtils.encrypt(key, contributorMap, encryptedContributorsFile)
  }

  /**
    * Running state of the actor, while in this state the actor can receive this messages:<br>
    * _AddCredentials: when the actor receives this message it tries to register a user to the system adding
    *                  the Profile passed as message parameter to his data structure.<br>
    * _UpdateCredentials: when the actor receives this message it tries to update a Profile <br>
    * _RemoveCredentials: when the actor receives this message it tries to remove the Profile
    *                     passed as parameter of the message<br>
    * _Authenticate: when the actor receives this message it checks it the credentials passed as parameter of
    *                the message are valid credentials already registered on the system<br>
    * _AddCollectionTo: when the actor receives this message it tries to add the collection passed as message
    *                   parameter to the user passed<br>
    * _RemoveCollectionFrom: when the actor receives this message it tries to remove the collection passed as message
    *                        parameter from the user passed<br>
    * _ListCollectionOf: when the actor receives this message it returns all the collections name<br>
    * _ListUsers: when the actor receives this message it returns the list of all the users registrated on the system.
    *
    * @param profiles: Set of Profiles representing the users stored in this actor
    */
  def running(profiles: Set[Profile]): Receive = {

    case message: AuthActorMessages => message match {

      /**
        * Clean the content of the profile set
        */
      case Clean => context become running(profiles.empty)

      /**
        * Persist data to disk using a defined encryption algorithm
        */
      case Save => persist(profiles)

      /**
        * Initialize data contained inside profile set, used when reading data
        * from disk during the boot of the system.
        *
        * @param username a String representing the username of the user
        * @param password a String representing the password of the user
        */
      case Init(username, password) =>{
        //context become running (profiles + Profile(username, password, Set.empty[ActorbaseCollection]))
        if(profiles.size > 0){
          println(" ho già gli utenti, coglione")
        }
        else{
          println("init message, adding "+username+" "+password)
          context become running (profiles + Profile(username, password, Set.empty[ActorbaseCollection]))
        }
      }


      /**
        * Add a pair username-password generating an hash to store the password,
        * togheter they represents credentials for a new user.
        *
        * @param username a String representing the username of the new user added
        * @param password a String representing the password of the new user added
        */
      case AddCredentials(username, password) =>
        val passwordCheck = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$""".r
        val check = passwordCheck findFirstIn password
        check map { p =>
          val salt = password.bcrypt(generateSalt)
          if (!profiles.contains(Profile(username, salt))) {
            sender ! "OK"
            persist(profiles + Profile(username, salt, Set.empty[ActorbaseCollection]))
            context become running (profiles + Profile(username, salt, Set.empty[ActorbaseCollection]))
          } else sender ! "UsernameAlreadyExists"
        } getOrElse sender ! "WrongCredentials"

      /**
        * Change the password associated to an user given his username and
        * current password
        *
        * @param username a String representing the username of the user owner of
        * the password designatef for change
        * @param password a String representing the current password of the user that will be updated
        * @param newPassword a String representing the new password to be set for
        * the requested username
        */
      case UpdateCredentials(username, password, newPassword) =>
        val passwordCheck = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$""".r
        val check = passwordCheck findFirstIn newPassword
        check map { p =>
          val optElem = profiles find (elem => (elem.username == username) && BCrypt.checkpw(password, elem.password))
          val salt = newPassword.bcrypt(generateSalt)
          optElem map { elem =>
            sender ! "OK"
            sender ! Stop
            persist(profiles - elem + elem.copy(password = salt))
            context become running (profiles - elem + elem.copy(password = salt))
          } getOrElse sender ! "UndefinedUsername"
        } getOrElse sender ! "WrongNewPassword"

      /**
        * Remove a pair username-password identified by a given username,
        *
        * @param username a String representing the username of the user to be removed
        */
      case RemoveCredentials(username) =>
        if (username != "admin") {
          val optElem = profiles find (_.username == username)
          optElem map { x =>
            val newProfile = x.copy(password = java.util.UUID.randomUUID().toString.bcrypt(generateSalt))
            persist(profiles - x + newProfile)
            sender ! "OK"
            context become running(profiles - x + newProfile)
          } getOrElse sender ! "UndefinedUsername"
        } else sender ! "UndefinedUsername"

      /**
        * Reset the password associated to an existing user, identified by his username
        *
        * @param username a String representing the username of the user designed for password reset
        */
      case ResetPassword(username) =>
        if (username != "admin") {
          val optElem = profiles find (_.username == username)
          optElem map { x =>
            val newProfile = x.copy(password = "Actorb4se".bcrypt(generateSalt))
            persist(profiles - x + newProfile)
            sender ! "OK"
            context become running(profiles - x + newProfile)
          } getOrElse sender ! "UndefinedUsername"
        } else sender ! "UndefinedUsername"

      /**
        * Authenticate a user by comparing a pair username-password with
        * previously stored inside profiles.
        *
        * @param username a String representing the username of the user to authenticate
        * @param password a String representing the password of the user to authenticate
        */
      case Authenticate(username, password) =>
        val optElem = profiles find (elem => (elem.username == username) && BCrypt.checkpw(password, elem.password))
        optElem map (_ => sender ! Some((username -> password))) getOrElse sender ! Some("None" -> "None")

      /**
        * Add a collection to a given user
        *
        * @param username a String representing the username of the user designed to get a new collection
        * @param collection a Collection reference serialized, represents the collection to be added to the user
        * @param permissions a Permission reference serialized, represents the permission level of the user against the new added collection
        * as contributor
        */
      case AddCollectionTo(username, collection, permissions) =>
        val optElem = profiles find (_.username == username)
        optElem map { x =>
          if (username != collection.getOwner)
            collection.addContributor(username, permissions)
          x.addCollection(collection)
          persist(profiles + x)
          sender ! "OK"
          context become running (profiles + x)
        } getOrElse sender ! "UndefinedUsername"

      /**
        * Remove a collection from a username
        *
        * @param username a String representing the username of the user designed for removal of the collection
        * @param collection a reference to a Collection, represents the collection to be added to the user
        */
      case RemoveCollectionFrom(username, collection) =>
        val optElem = profiles find (_.username == username)
        optElem map { x =>
          if (x.contains(collection)) {
            collection.removeContributor(username)
            x.removeCollection(collection)
            profiles.foreach { p =>
              p.getCollections map { c => if (c.containsReadContributor(username) || c.containsReadWriteContributor(username)) c.removeContributor(username) }
              if (p.contains(collection)) p.removeCollection(collection)
            }
            persist(profiles + x)
            sender ! "OK"
            context become running (profiles + x)
          }
        } getOrElse sender ! "UndefinedUsername"

      /**
        * Build a list of collection names and reply it to the sender
        *
        * @param owner a String representing the owner of the requested collection name list
        */
      case ListCollectionsOf(username) =>
        val optElem = profiles find (_.username == username)
        optElem map { set =>
          val names = set.getCollections map (collection => Map(collection.getOwner -> List(collection.getName, collection.getWeight.toString)))
          sender ! ListTupleResponse(names.toList)
        }

      /**
        * Build a list of UUIDs of collections owned by the username
        * that request them
        *
        * @param username a String representing the username of the user designed for retrieval of his collections
        */
      case ListUUIDsOwnedBy(username) =>
        val optElem = profiles find (_.username == username)
        optElem map { set =>
          val uuids =
            if (username == "admin") set.getCollections map (collection => collection.getUUID)
            else set.getCollections map (collection => if (collection.getOwner == username) collection.getUUID else "")
          sender ! ListResponse(uuids.toList)
        }

      /**
        * Update collection weight
        */
      case SetCollectionWeightOf(collection, weight) =>
        profiles filter ( x => x.contains(collection) ) map { x =>
          x.getCollections map { c =>
            if (c == collection) {
              c.setWeight(weight)
            }
          }
        }

      /**
        * Return all users contained in the system as a List[String]
        */
      case ListUsers =>
        var users = List.empty[String]
        profiles map (profile => users ::= profile.username)
        sender ! ListResponse(users)

      /**
        * Command warehouseman to persist data to disk using a defined
        * encryption algorithm. Sends a message to the distributed pub sub, every
        * warehouseman actor is subscribed to the topic "persist-data" so it receive
        * the message sent.
        */
      case PersistDB => mediator ! Publish("persist-data", Persist)
    }
  }
}

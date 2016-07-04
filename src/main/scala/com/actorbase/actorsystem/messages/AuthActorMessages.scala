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

package com.actorbase.actorsystem.messages.AuthActorMessages

import com.actorbase.actorsystem.utils.ActorbaseCollection
import com.actorbase.actorsystem.utils.ActorbaseCollection.Permissions

/**
  * Trait that contains all the messages processable from the AuthActor
  */
sealed trait AuthActorMessages

/**
  * Message used to ask to the AuthActor to return a list containing all the users of the system
  */
case object ListUsers extends AuthActorMessages

/**
  * Message used to persist data contained in memory to disk
  */
case object Save extends AuthActorMessages

/**
  * Message used to ask to the AuthActor the deletion of all in-memory contents
  */
case object Clean extends AuthActorMessages

/**
  * Message used to send to the distributed pub-sub a broadcast message of
  * persistence of the data contained inside the storekeeper actors to the disk case
  */
case object PersistDB extends AuthActorMessages

/**
  * Message used to ask at AuthActor to add new Credentials
  * @param username a string with username of the new credential to add
  * @param password a string with the password of the user that will add
  */
final case class AddCredentials(username: String, password: String) extends AuthActorMessages

/**
  * Message used to ask at AuthActor to update an existing Credentials
  * @param username a string with username of the credential to update
  * @param password a string with the old password of the credential that will be update, needed to authenticate it
  * @param newPassword a string with the new password that will replace old password
  */
final case class UpdateCredentials(username: String, password: String, newPassword: String) extends AuthActorMessages

/**
  * Message used to remove an existing credential
  * @param username a string with the username to remove
  */
final case class RemoveCredentials(username: String) extends AuthActorMessages

/**
  * Message used to reset the password of an existing credential
  * @param username a string with the username designed for password reset
  */
final case class ResetPassword(username: String) extends AuthActorMessages

/**
  * Message used to do an authentication request
  * @param username a string with the username for the authentication request
  */
final case class Authenticate(username: String, password: String) extends AuthActorMessages

/**
  * Message used to the associate collection to an user
  * @param username a string with the username to associate at the collection
  * @param collection the collection to associate at the username
  */
final case class AddCollectionTo(username: String, collection: ActorbaseCollection, permissions: Permissions) extends AuthActorMessages

/**
  * Message used to the dissociate collection to an user
  * @param username a string with the username to dissociate from collection
  * @param collection the collection to dissociate from the username
  */
final case class RemoveCollectionFrom(username: String, collection: ActorbaseCollection) extends AuthActorMessages

/**
  * Message used to list the collection associated with the username passed as parameter
  * @param username a string with the username of the associated collection that will be displayed
  */
final case class ListCollectionsOf(username: String) extends AuthActorMessages

/**
  * Message used to init the Authactor with all users
  *
  * @param username a String representing the username of the user
  * @param password a String representing the password of the user
  */
final case class Init(username: String, password: String) extends AuthActorMessages

/**
  * Message used to request a list of collections owned by a given user
  *
  * @param username a String representing the username of the user
  */
final case class ListUUIDsOwnedBy(username: String) extends AuthActorMessages

/**
  * Message used to update weight of a collection
  */
final case class SetCollectionWeightOf(collection: ActorbaseCollection, weight: Long) extends AuthActorMessages

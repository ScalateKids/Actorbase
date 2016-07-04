/**
  * The MIT License (MIT)
  * <p/>
  * Copyright (c) 2016 ScalateKids
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the \"Software\"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
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

package com.actorbase.actorsystem.messages.MainMessages

import akka.actor.ActorRef
import akka.dispatch.ControlMessage

import com.actorbase.actorsystem.utils.ActorbaseCollection
import com.actorbase.actorsystem.utils.ActorbaseCollection.Permissions

/**
  * Trait defining a generic main actor dedicated message
  */
sealed abstract trait MainMessage

/**
  * Message used to request to do an insertion operation in a collection
  * @param requester the username of who made the request
  * @param collection the collection where data will be Insert
  * @param key the key of insertion data
  * @param value the data that will be inserted
  * @param update boolean flag to allow overwrite
  */
final case class InsertTo(requester: String, collection: ActorbaseCollection, key: String, value: Array[Byte], update: Boolean = false) extends MainMessage

/**
  * Message used to get data from a collection
  * @param requester the username of who made the request
  * @param collection the collection where the data will be got
  * @param key the key of the value requested
  */
final case class GetFrom(requester: String, collection: ActorbaseCollection, key: String = "") extends MainMessage

/**
  * Message used to send pieces of collection from storekeeper to main for collect it and resend to client
  * @param clientRef reference of who will receive the response from storekeeper
  * @param collection the collection that contain the pieces to send
  * @param key key of pieces to collect
  */
final case class CompleteTransaction(requester: String, clientRef: ActorRef, collection: ActorbaseCollection, items: Map[String, Array[Byte]]) extends MainMessage with ControlMessage

/**
  * Message used to remove item from collection
  * @param requester a string with username of who made the remove request
  * @param uud the id of the collection where data will be removed
  * @param key the key of the data that will be removed
  */
final case class RemoveFrom(requester: String, uuid: String, key: String = "") extends MainMessage

/**
  * Message used to add contributor in a collection
  * @param requester string with username of who made the request
  * @param username username to add at the collection contributor
  * @param permission the permission of the new user in the collection
  * @param uuid the id of the collection where the collaborator will added
  */
final case class AddContributor(requester: String, username: String, permission: Permissions, uuid: String) extends MainMessage

/**
  * Message used to remove contributor from a collection
  * @param requester string with username of who made the request
  * @param username string with username that will be removed from contributor list of the collection
  * @param uuid string with collection id where the contributor will be removed
  */
final case class RemoveContributor(requester: String, username: String, uuid: String) extends MainMessage

/**
  * Message used to create a new collection in actorbase
  * @param collection the new collection that will be added to actorbase system
  */
final case class CreateCollection(requester: String, collection: ActorbaseCollection) extends MainMessage

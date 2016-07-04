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

package com.actorbase.actorsystem.messages.StorefinderMessages

import akka.actor.ActorRef
import akka.dispatch.ControlMessage

/**
  * Trait defining a generic storefinder actor dedicated message
  */
sealed abstract trait StorefinderMessage

/**
  * Message used to request to get all item from the Storefinder
  *
  * @param requester a String representing the requester of the items
  */
final case class GetAllItems(requester: String) extends StorefinderMessage

/**
  * Message used to update the collection size after an insert or a remove operation
  * @param increment boolean flag to do an increment or decrement operation with size (true = increment ; false = decrement)
  */

final case class UpdateCollectionSize(valueSize: Long, increment: Boolean = true) extends StorefinderMessage with ControlMessage

/**
  * Message used to get data from the collection associated at the storefinder
  * @param key string with value key that will be search in the collection associated
  */
final case class Get(key: String) extends StorefinderMessage

/**
  * Message used to receive shards of collection from storekeepers to return to a client
  * @param clientRef reference to client that made the request and will receive the response
  * @param items map that contain the shards of collection to return
  */
final case class PartialMapTransaction(requester: String, clientRef: ActorRef, items: Map[String, Array[Byte]]) extends StorefinderMessage with ControlMessage

/**
  * Message used to remove data from the collection associated at the storefinder
  * @param key a string with the key of the value that will be trashed
  */
final case class Remove(key: String) extends StorefinderMessage

/**
  * Message used to insert value into the collection associated to the storefinder
  * @param key a string with the key of value that will be inserted in the collection
  * @param value data to insert in the collection
  * @param update boolean flag to allow overwrite data o deny it (true = allow ; false = deny)
  */
final case class Insert(key: String, value: Array[Byte], update: Boolean = false) extends StorefinderMessage

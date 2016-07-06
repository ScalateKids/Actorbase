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
  * @author Scalatekids
  * @version 1.0
  * @since 1.0
  */

package com.actorbase.actorsystem.actors.warehouseman

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.config.ConfigFactory
import java.io.File

import com.actorbase.actorsystem.messages.WarehousemanMessages._
import com.actorbase.actorsystem.utils.CryptoUtils

object Warehouseman {

  def props(s: String) : Props = Props(classOf[Warehouseman], s)

}

class Warehouseman(collectionUUID: String = "namecollection-owner") extends Actor with ActorLogging {

  private val config = ConfigFactory.load().getConfig("persistence")
  private val wareUUID = java.util.UUID.randomUUID.toString
  private val rootFolder = config getString "save-folder"

  override def postStop: Unit = {
    log.debug("Cleaning directory")
    new File(rootFolder + collectionUUID + "/" + wareUUID + ".actb").delete()
    new File(rootFolder + collectionUUID + "/collection-meta.actbmeta").delete()
  }

  /**
    * Receive method of the Warehouseman actor, it does different things based on the message it receives:<br>
    * _Init: when the actor receives this message it inserts the item in the collection requested by the user.<br>
    * _Save: when the actor receives this message it Save a shard of a collection represented by the TreeMap stored by a storekeeper <br>
    * _Clean: when the actor receives this message it Delete a file with the Range with the keys passed in<br>
    * _Read: when the actor receives this message it Read a file from filesystem and decrypt the content extracting the map shard contained</br>
    *
    */
  def receive = {

    case message: WarehousemanMessage => message match {

      /**
        * Initialize collection by name of the collection and his howner
        * @param collection name of the collection to initialize
        * @param owner owner's collection name
        */
      case Init(collection, owner) =>
        val key = config getString("encryption-key")
        val encryptedMetaFile = new File(rootFolder + collectionUUID + "/collection-meta.actbmeta")
        if (!encryptedMetaFile.exists) {
          encryptedMetaFile.getParentFile.mkdirs
          if (owner != "anonymous")
            CryptoUtils.encrypt(key, Map("collection" -> collection, "owner" -> owner), encryptedMetaFile)
        }

      /**
        * Persist data to disk, encrypting with pre-defined encryption algorithm
        *
        * @param map a Map containing key-values to persist to disk
        */
      case Save(map) =>
        log.debug("warehouseman: save " + rootFolder + collectionUUID + "/" + wareUUID + ".actb")
        val key = config getString("encryption-key")
        val encryptedShardFile = new File(rootFolder + collectionUUID + "/" + wareUUID + ".actb")
        encryptedShardFile.getParentFile.mkdirs
        CryptoUtils.encrypt(key, map, encryptedShardFile, false)
        sender ! 0 // ok reply

      /**
        * Read a file from filesystem and decrypt the content
        * extracting the map shard contained
        *
        * @param f an encrypted file containing the shard of the collection
        */
      case Read(f) =>
        log.debug("warehouseman: read")
        val key = config getString("encryption-key")
        val m = CryptoUtils.decrypt[Map[String, Any]](key, f)
        sender ! m // ok reply

      /**
        * Delete a file with the Range with the keys passed in
        *
        * @param range a KeyRange representing the range of the file to delete
        */
      case Clean =>
        log.debug("Cleaning directory")
        new File(rootFolder + collectionUUID + "/" + wareUUID + ".actb").delete()
        new File(rootFolder + collectionUUID + "/collection-meta.actbmeta").delete()

    }
  }
}

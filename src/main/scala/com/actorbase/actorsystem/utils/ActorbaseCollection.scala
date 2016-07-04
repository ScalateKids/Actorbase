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

package com.actorbase.actorsystem.utils

import scala.math.Ordered.orderingToOrdered

case object ActorbaseCollection {

  /**
    * Trait defining generic permission type
    */
  sealed trait Permissions

  /**
    * Define permission of type read-only
    */
  case object Read extends Permissions

  /**
    * Define permission of type readwrite
    */
  case object ReadWrite extends Permissions
}

/**
  * Class representing a collection of actorbase
  *
  * @param name a String representing the name of the collection
  * @param owner a String representing the username of the owner of this collection
  */
case class ActorbaseCollection (private var name: String,
  private var owner: String,
  private var size: Int = 0,
  private var weight: Long = 0) extends Ordered[ActorbaseCollection] {

  private val uuid: String = owner + name
  private var contributors = Map[String, ActorbaseCollection.Permissions]("admin" -> ActorbaseCollection.ReadWrite)

  /**
    * @return a String representing the name of the collection
    */
  def getName: String = name

  /**
    * @return a String representing the username of the owner of this collection
    */
  def getOwner: String = owner

  /**
    * @return a String representing a universal-unique-identified ID
    */
  def getUUID: String = uuid

  /**
    * @return the size of the collection
    */
  def getSize: Int = size

  /**
    * @return the weight of the collection
    */
  def getWeight: Long = weight

  /**
    * Return a map containing contributors and their permission on the collection
    *
    * @return a Map of String -> Boolean, representing contributors username associated to their
    * permission on the collection
    */
  def getContributors: Map[String, ActorbaseCollection.Permissions] = contributors

  /**
    * Check if the user passed as param has read permission
    * on this actorbasecollection
    *
    * @param contributor: the username of the user that has to be checked
    * @return true if the user has read permission on the actorbasecollection
    *         false otherwise
    */
  def containsReadContributor(contributor: String): Boolean = {
    var contains = false
    contributors get (contributor) map { c =>
      c match {
        case ActorbaseCollection.Read => contains = true
        case ActorbaseCollection.ReadWrite => contains = false
      }
    } getOrElse (contains = false)
    contains
  }

  /**
    * Check if the user passed as param has read and write permission
    * on this actorbasecollection
    *
    * @param contributor: the username of the user that has to be checked
    * @return true if the user has read and write permission on the actorbasecollection
    *         false otherwise
    */
  def containsReadWriteContributor(contributor: String): Boolean = {
    var contains = false
    contributors get (contributor) map { c =>
      c match {
        case ActorbaseCollection.Read => contains = false
        case ActorbaseCollection.ReadWrite => contains = true
      }
    } getOrElse (contains = false)
    contains
  }

  /**
    * Add a contributor to this actorbase collection
    *
    * @param username: the username of a user that has to be added as a contributor
    * @param permission: read or read and write permission
    */
  def addContributor(username: String, permission: ActorbaseCollection.Permissions): Unit = contributors += (username -> permission)

  /**
    * Remove a contributor from this actorbase collection
    *
    * @param username: the username of a user that has to removed from the contributors
    */
  def removeContributor(username: String): Unit = contributors -= username

  /**
    * Increment the size of this actorbasecollection by 1
    */
  def incrementSize = size += 1

  /**
    * Decrement the size of this actorbasecollection by 1
    */
  def decrementSize = size -= 1

  /**
    * Increment the weight of this actorbasecollection by a given value
    *
    * @param w a Long value to be added
    */
  def incrementWeight(w: Long) = weight += w

  def setWeight(w: Long) = weight = w
  /**
    * Decrement the weight of this actorbasecollection by a given value
    *
    * @param w a Long value to be removed
    */
  def decrementWeight(w: Long) = weight -= w

  /**
    * override the compare method of comparable trait. This methods
    * compares two actorbasecollections object
    *
    * @param that: the actorbasecollection that has to be compared with this
    * @return an integer > 0 if this collection is > than that one
    < 0 if that collection is > than this one
    = 0 if the collections are the same
    */
  override def compare(that: ActorbaseCollection): Int = {
    (this.name + this.owner) compare (that.name + that.owner)
  }

  /**
    * override the euqals method of the Any trait. This methods
    * compares two actorbasecollections objects.
    *
    * @param o: Any object, if the object passed is not an Actorbasecollection the method return false
    * @return true if the param passed is the same object as this Actorbasecollection, false otherwise
    */
  override def equals(o: Any) = o match {
    case that: ActorbaseCollection => that.uuid.equals(this.uuid)
    case _ => false
  }

  /**
    * Method that overrides the Any class method hashCode
    *
    * @return a String representing the hashCode
    */
  override def hashCode = uuid.hashCode

}

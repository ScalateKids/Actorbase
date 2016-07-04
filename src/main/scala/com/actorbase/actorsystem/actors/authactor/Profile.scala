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

import com.actorbase.actorsystem.utils.ActorbaseCollection

case object Profile {
  def apply(u: String, p: String): Profile = Profile(u, p, Set.empty[ActorbaseCollection])
}

/**
  * Class that models a user of actorbase. It contains the credentials of the user and
  * a Set with all the ActorbaseCollection created by him
  */
case class Profile(username: String, password: String, private var collections: Set[ActorbaseCollection]) {

  /**
    * Method that returns all the collections created by this user
    *
    * @return a Set containing all the ActorbaseCollection created by this users
    */
  def getCollections: Set[ActorbaseCollection] = collections

  /**
    * Method that adds an ActorbaseCollection from this Profile
    *
    * @param collection: an ActorbaseCollection to add
    * @return no return value
    */
  def addCollection(collection: ActorbaseCollection): Unit = collections += collection

  /**
    * Method that removes an ActorbaseCollection from this Profile
    *
    * @param collection: the ActorbaseCollection to remove
    * @return no return value
    */
  def removeCollection(collection: ActorbaseCollection): Unit = {
    if (collections.contains(collection)) {
      collections -= collection
    }
  }

  /**
    * Method that checks if the ActorbaseCollection passed as param is contained in the Set of ActorbaseCollections
    * of this Profile
    *
    * @param collection: an ActorbaseCollection that will be searched in the Set of ActorbaseCollections of this Profile
    * @return true if the Profile got that collection, false otherwise
    */
  def contains(collection: ActorbaseCollection): Boolean = collections.contains(collection)

  /**
    * Method that checks if the Object passed as param is the same Profile as this object
    *
    * @param o: Any object to check if represent the same Profile as this object
    * @return true if the param is the same Profile as this object, false otherwise
    */
  override def equals(o: Any) = o match {
    case that: Profile => that.username.equals(this.username)
    case _ => false
  }

  /**
    * Method that overrides the Any class method hashCode
    *
    * @return a String representing the hashCode
    */
  override def hashCode = username.hashCode

}

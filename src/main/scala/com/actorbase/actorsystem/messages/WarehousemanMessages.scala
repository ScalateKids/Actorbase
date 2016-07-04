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

package com.actorbase.actorsystem.messages.WarehousemanMessages

import java.io.File

/**
  * Trait defining a generic warehouseman dedicated message
  */
sealed abstract trait WarehousemanMessage

/**
  * Message used to ask to warehouseman to clean data on disk
  */
final case object Clean extends WarehousemanMessage

/**
  * Message used to initialize collection on drive
  * @param collection name of collection to store
  * @param owner string with name of the collection owner
  */
final case class Init(collection: String, owner: String) extends WarehousemanMessage

/**
  * Message used to recall a save operation on drive
  * @param map map with data to save on permanent memory
  */
final case class Save(map: Map[String, Array[Byte]]) extends WarehousemanMessage

/**
  * Message used to ask at warehouseman to read data from permanent memory
  * @param file file that warehouseman will read
  */
final case class Read(file: File) extends WarehousemanMessage

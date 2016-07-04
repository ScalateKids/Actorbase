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

package com.actorbase.actorsystem.messages.ClientActorMessages

import spray.json._
import spray.json.DefaultJsonProtocol._

final case class ListTupleResponse(tuples: List[Map[String, List[String]]])

case object ListTupleResponse {
  implicit val goJson = jsonFormat1(ListTupleResponse.apply)
}

/**
  * A message for list the responses
  * @param list the list that contain responses
  */
final case class ListResponse(list: List[String])

case object ListResponse {
  implicit val goJson = jsonFormat1(ListResponse.apply)
}
/**
  * Message that represent a server response
  * @param response the response of the server in byteArray
  */
final case class Response(response: Any)

case object Response {

  implicit object AnyJsonFormat extends JsonFormat[Any] {

    def write(x: Any) = x match {
      case n: Int => JsNumber(n)
      case d: Double => JsNumber(d)
      case i: BigInt => JsNumber(i)
      case l: Long => JsNumber(l)
      case s: String => JsString(s)
      case x: Seq[_] => seqFormat[Any].write(x)
      case m: Map[String, _] => mapFormat[String, Any].write(m)
      case b: Boolean if b == true => JsTrue
      case b: Boolean if b == false => JsFalse
      case a: Array[Byte] => arrayFormat[Byte].write(a)
      case x => serializationError("Do not understand object of type " + x.getClass.getName)
    }

    def read(value: JsValue) = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case a: JsArray => listFormat[Any].read(value)
      case o: JsObject => mapFormat[String, Any].read(value)
      case JsTrue => true
      case JsFalse => false
      case x => deserializationError("Do not understand how to deserialize " + x)
    }
  }
  implicit val goJson = jsonFormat1(Response.apply)
}

/**
  * Message with a map of structured responses
  * @param collection the collection of the map responses
  * @param map the map with the responses
  */
final case class MapResponse(owner: String, collectionName: String, contributors: Map[String, Boolean], data: Map[String, Any])

object MapResponse {

  implicit object AnyJsonFormat extends JsonFormat[Any] {

    def write(x: Any) = x match {
      case n: Int => JsNumber(n)
      case d: Double => JsNumber(d)
      case i: BigInt => JsNumber(i)
      case l: Long => JsNumber(l)
      case s: String => JsString(s)
      case x: Seq[_] => seqFormat[Any].write(x)
      case m: Map[String, _] => mapFormat[String, Any].write(m)
      case b: Boolean if b == true => JsTrue
      case b: Boolean if b == false => JsFalse
      case a: Array[Byte] => arrayFormat[Byte].write(a)
      case x => serializationError("Do not understand object of type " + x.getClass.getName)
    }

    def read(value: JsValue) = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case a: JsArray => listFormat[Any].read(value)
      case o: JsObject => mapFormat[String, Any].read(value)
      case JsTrue => true
      case JsFalse => false
      case x => deserializationError("Do not understand how to deserialize " + x)
    }

  }

  implicit val goJson = jsonFormat4(MapResponse.apply)
}

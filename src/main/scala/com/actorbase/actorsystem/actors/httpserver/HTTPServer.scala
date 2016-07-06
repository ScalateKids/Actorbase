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

package com.actorbase.actorsystem.actors.httpserver

import akka.actor.{Actor, ActorSystem, ActorLogging, ActorRef, PoisonPill, Props, Terminated, OneForOneStrategy}
import akka.actor.SupervisorStrategy._
import akka.io.IO
import spray.can.Http
import akka.event.LoggingReceive

import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.cluster._
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.typesafe.config.ConfigFactory

import com.actorbase.actorsystem.actors.clientactor.ClientActor
import com.actorbase.actorsystem.actors.authactor.AuthActor
import com.actorbase.actorsystem.messages.AuthActorMessages.{ Init, Save, Clean }
import com.actorbase.actorsystem.actors.main.Main
import com.actorbase.actorsystem.messages.MainMessages._
import com.actorbase.actorsystem.utils.ActorbaseCollection
import com.actorbase.actorsystem.utils.ActorbaseCollection. { Permissions, Read, ReadWrite }
import com.actorbase.actorsystem.utils.CryptoUtils

import scala.collection.mutable.Queue
import java.io.File

/**
  * Class that represent a HTTPServer actor. This actor is responsible to accept the connection
  * incoming from clients and to instantiate a ClientActor assigned to the client asking.
  *
  * @param main: an ActorRef to a main actor
  * @param authProxy: an ActorRef to the AuthActor
  * @param address: a String representing the address on which Actorbase has to listen on
  * @param listenPort: a Int representing the port on which Actorbase has to listen on
  */
class HTTPServer(main: ActorRef, authProxy: ActorRef, address: String, listenPort: Int) extends Actor
    with ActorLogging with SslConfiguration {

  val config = ConfigFactory.load().getConfig("persistence")
  implicit val system = context.system
  IO(Http)(system) ! Http.Bind(self, interface = address, port = listenPort)

  val initLoad: Unit = loadData

  /**
    * Method that overrides the supervisorStrategy method.
    */
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception => Resume
    }

  /**
    * Loads all the data saved on the rootfolder onto the system. This is used to repopulate
    * the database after a restart.
    * The actor reads all the data from files and it proceed to send messages to the right actors
    * to repopulate the server.
    *
    * @return no return value
    */
  def loadData: Unit = {
    val root = new File(config getString "save-folder")
    var dataShard = Map.empty[String, Array[Byte]]
    var usersmap = Map.empty[String, String]
    var contributors = Map.empty[String, List[(String, Boolean)]]
    var data = Queue.empty[(ActorbaseCollection, Map[String, Array[Byte]])]
    log.debug("LOADING ......... ")
    if (root.exists && root.isDirectory) {
      var (name, owner) = ("", "")
      root.listFiles.filter(_.isDirectory).foreach { x =>
        x.listFiles.filter(_.isFile).foreach { x =>
          x match {
            case meta if meta.getName.endsWith("actbmeta") =>
              val metaData = CryptoUtils.decrypt[Map[String, String]](config getString "encryption-key", meta)
              metaData get "collection" map (c => name = c)
              metaData get "owner" map (o => owner = o)
              main ! CreateCollection(owner, ActorbaseCollection(name, owner))
            case user if (user.getName == "usersdata.shadow") =>
              usersmap ++= CryptoUtils.decrypt[Map[String, String]](config getString "encryption-key", user)
            case contributor if (contributor.getName == "contributors.shadow") =>
              contributors ++= CryptoUtils.decrypt[Map[String, List[(String, Boolean)]]](config getString "encryption-key", contributor)
            case _ => dataShard ++= CryptoUtils.decrypt[Map[String, Array[Byte]]](config getString "encryption-key", x)
          }
        }
        val collection = ActorbaseCollection(name, owner)
        data += (collection -> dataShard)
        dataShard = dataShard.empty
      }

      //authProxy ! Clean

      usersmap map ( x => authProxy ! Init(x._1, x._2) )

      contributors.foreach {
        case (k, v) =>
          v.foreach { item =>
            val permission = if (item._2) ReadWrite else Read
            main ! AddContributor("admin", k, permission, item._1)
          }
      }

      contributors = contributors.empty
      // populate collections
      data.foreach {
        case (k, v) =>
          v.foreach {
            case (kk, vv) =>
              main ! InsertTo(k.getOwner, k, kk, vv, false)
          }
      }

      def getRecursively(f: File): Seq[File] = f.listFiles.filter(_.isDirectory).flatMap(getRecursively) ++ f.listFiles
      getRecursively( root ).foreach { f =>
        if (!f.getName.endsWith("shadow") && f.getName != "usersdata")
          f.delete()
      }

    } else {
      log.debug("Directory not found!")
      import com.github.t3hnar.bcrypt._
      authProxy ! Init("admin", "Actorb4se".bcrypt(generateSalt))
    }

    authProxy ! Save

  }

  /**
    * Receive method, handle connection from outside, registering it to a
    * dedicated actor
    *
    */
  def receive: Receive = LoggingReceive {
    case _: Http.Connected =>
      val serverConnection = sender()
      val handler = context.actorOf(Props(new ClientActor(main, authProxy)))
      serverConnection ! Http.Register(handler)

    case Terminated(_) =>
  }

}

/**
  * HTTPServer object, it contains the main of the application
  */
object HTTPServer {
  def main(args: Array[String]) = {
    var (hostname, port) = ("127.0.0.1", 2500)
    //Argument GrammarParser
    if (args.length != 0)
    {
      val arglist = args.toList
      type OptionMap = Map[String, String]

      def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
        def isSwitch(s : String) = (s(0) == '-')
        list match {
          case Nil => map
          case "-h" :: value :: tail =>
            nextOption(map ++ Map("host" -> value), tail)
          case "-p" :: value :: tail =>
            nextOption(map ++ Map("port" -> value), tail)
          case string :: Nil => nextOption(map ++ Map("error" -> string), list.tail)
          case _ :: value :: tail =>
            nextOption(map ++ Map("error" -> value), tail)
        }
      }
      val options = nextOption(Map(), arglist)
      options get "host" map (hostname = _)
      options get "port" map (s => port = s.toInt)
    }
    val config = ConfigFactory.parseString(s"""
akka.remote.netty.tcp.hostname=${hostname}
akka.remote.netty.tcp.port=${port}
listen-on=${hostname}
""").withFallback(ConfigFactory.load())
    val system = ActorSystem(config getString "name", config)
    // singleton authactor
    system.actorOf(ClusterSingletonManager.props(
      singletonProps = Props(classOf[AuthActor]),
      terminationMessage = PoisonPill,
      settings = ClusterSingletonManagerSettings(system)),
      name = "authactor")
    // proxy
    val authProxy = system.actorOf(ClusterSingletonProxy.props(
      singletonManagerPath = "/user/authactor",
      settings = ClusterSingletonProxySettings(system)),
      name = "authProxy")
    // main sharding
    ClusterSharding(system).start(
      typeName = Main.shardName,
      entityProps = Main.props(authProxy),
      settings = ClusterShardingSettings(system),
      extractShardId = Main.extractShardId,
      extractEntityId = Main.extractEntityId)

    val main = ClusterSharding(system).shardRegion(Main.shardName)
    val http = system.actorOf(Props(classOf[HTTPServer], main, authProxy, config getString "listen-on", config getInt "exposed-port"))
  }
}

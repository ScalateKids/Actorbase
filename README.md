[![Build Status](https://travis-ci.org/ScalateKids/Actorbase-Server.svg?branch=master)](https://travis-ci.org/ScalateKids/Actorbase-Server)

<div align="center">
 <img src="https://github.com/ScalateKids/Actorbase-Documents/blob/master/img/ablogomd.png" />

</div>
 </br>

Actorbase is a key-value NoSQL database build upon the actor model for concurrency, hence the name "Actorbase".

## Overview

### The actor model

The actor model is a well known mathematical model, proposed by Carl Hewitt in the 1973, that abstract concurrent and distributed programming into actors.

Actors are entity that are capable of receiving messages, respond to other actors requests and create new actors if necessary.
During its lifetime an actor can change its interface, hence change the type of messages it can handle.

### NoSQL Database

NoSQL (not only SQL) encompasses all the technologies developed for the databases in response to the demand of modern applications, i.e. the management of big amount of data.

Relational databases fails to guarantee ACID properties while scaling in a distributed fashion.

### Actorbase

Actorbase, as stated before, aims to be a Key-value NoSQL database that follows the actor model.

## Design
<p align="center">
<img src="https://github.com/ScalateKids/Actorbase-Documents/blob/master/img/RQ/DevManual/ClusterPmd.png">
</p>
</br>

Actorbase is an application conceived to be used in a distributed fashion,
balancing load between multiple nodes as shown above.

For every incoming connection a dedicated actor, named clientactor,
is spawned;

it is responsible for all incoming requests from the
client that is connected, and his main role is to forward these requests directly to the
main actors equally distributed across the nodes of a
cluster.

In this way, with main actors breeding
their own hierarchy of subordinates, all incoming data is spread across the
cluster network.

## Quick start

The project is built using sbt, which is available on Linux, OSX and Windows as
well. Just clone the repository and run it
```sh
$ git clone https://github.com/ScalateKids/Actorbase.git
$ cd Actorbase
$ sbt "run-main com.actorbase.actorsystem.actors.httpserver.HTTPServer -h
<hostname> -p <port>"
```
Running just sbt run will result in a fallback to localhost and 9999 as port,
it is also possible to set exposed port to listen for connections and specify
different seeds:
```sh
$ sbt "run-main com.actorbase.actorsystem.actors.httpserver.HTTPServer -h
<hostname> -p <port>" -Dexposed-port=<listening-port>
-Dseed-host=<seed-hostname>
```
Finally it is possible to build a fat jar
```sh
$ sbt assembly
$ java -jar actorbase.jar -h <hostname> -p <port>
```
missing values for hostname and port will result in a fallback (localhost as hostname and 9999 as port)

For further details about configuration, see the wiki:

## License

See the [LICENSE](LICENSE.md) file for license rights and limitations (MIT).

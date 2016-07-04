[![Build Status](https://travis-ci.org/ScalateKids/Actorbase-Server.svg?branch=master)](https://travis-ci.org/ScalateKids/Actorbase-Server)


![alt text][logo]

[logo]: https://github.com/ScalateKids/Actorbase-Documents/blob/master/img/ablogomd.png

Created by Scalatekids.

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

## Debug instructions
To lauch actorbase as a cluster application
### Node A
```sh
$ sbt run -Dlisten-on=<ip-address>
```
### Node B
```sh
$ sbt run -Dlisten-on=<ip-address> -Dexposed-port=9998 -Dclustering-port=2501
```
### Server-side configuration
```sh
$ sbt
```

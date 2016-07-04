[![Build Status](https://travis-ci.org/ScalateKids/Actorbase-Server.svg?branch=master)](https://travis-ci.org/ScalateKids/Actorbase-Server)

# Actorbase-Server
This is the server side of the Actorbase application.

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

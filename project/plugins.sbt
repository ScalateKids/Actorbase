logLevel := Level.Warn
resolvers += Resolver.url("sbt-plugin-releases-scalasbt", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-proguard" % "0.2.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

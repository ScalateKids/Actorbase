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

package com.actorbase.actorsystem.actors.httpserver

import com.typesafe.config.{ Config, ConfigFactory }
import java.security.{SecureRandom, KeyStore}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import spray.io._

/**
  * SSL configuration, should be enabled in application.conf 
  */
trait SslConfiguration {

  def sslConfig: Config = ConfigFactory.load().getConfig("ssl")

  /**
    * Custom SSL configuration, currently using a sample CA inside resources
    *
    * if there is no SSLContext in scope implicitly the HttpServer uses the
    * default SSLContext, since we want non-default settings in this example we
    * make a custom SSLContext available here
    *
    * @return a SSLContext value
    */
  implicit def sslContext: SSLContext = {
    val keyStoreResource = sslConfig getString "certificate-file"
    val password = sslConfig getString "certificate-password"

    val keyStore = KeyStore.getInstance("JKS")
    val in = getClass.getClassLoader.getResourceAsStream(keyStoreResource)
    require(in != null, "Bad java key storage file: " + keyStoreResource)
    keyStore.load(in, password.toCharArray)

    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }

  /**
    * If there is no ServerSSLEngineProvider in scope implicitly the HttpServer
    * uses the default one, since we want to explicitly enable cipher suites and
    * protocols we make a custom ServerSSLEngineProvider available here
    * @return a ServerSSLEngineProvider
    */
  implicit def sslEngineProvider: ServerSSLEngineProvider = {
    ServerSSLEngineProvider { engine =>
      engine.setEnabledProtocols(Array("TLSv1", "TLSv1.2", "TLSv1.1"))
      engine
    }
  }
}

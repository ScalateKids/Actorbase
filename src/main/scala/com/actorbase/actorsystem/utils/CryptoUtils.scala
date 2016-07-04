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

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, File, FileInputStream, FileOutputStream, IOException, ObjectInputStream, ObjectOutputStream }
import java.security.{InvalidKeyException, NoSuchAlgorithmException}
import java.util.zip.{ DataFormatException, Deflater, DeflaterOutputStream, Inflater, InflaterInputStream }
import javax.crypto.{BadPaddingException, Cipher, IllegalBlockSizeException, NoSuchPaddingException}
import javax.crypto.spec.SecretKeySpec
import com.typesafe.config.ConfigFactory

/**
  * Cryptography service object, this object is used to crypt data before saving
  * it into a file and to decrypt data after is read from files.
  */
object CryptoUtils {

  val config = ConfigFactory.load().getConfig("persistence")
  /** configuration init */
  val Algorithm = config getString "encryption-algorithm"
  val Transformation = config getString "encryption-algorithm"

  @throws(classOf[IOException])
  def compress(data: Array[Byte]): Array[Byte] = {
    val deflater = new Deflater()
    deflater.setInput(data);
    val outputStream = new ByteArrayOutputStream(data.length)
    deflater.finish()
    val buffer  = new Array[Byte](128)
    while (!deflater.finished()) {
      // val count = deflater.deflate(buffer) // returns the generated code... index
      outputStream.write(buffer, 0, deflater.deflate(buffer))
    }
    outputStream.close()
    outputStream.toByteArray()
  }

  @throws(classOf[IOException])
  @throws(classOf[DataFormatException])
  def decompress(data: Array[Byte]): Array[Byte] = {
    val inflater = new Inflater()
    inflater.setInput(data)
    val outputStream = new ByteArrayOutputStream(data.length)
    val buffer = new Array[Byte](1024)
    while (!inflater.finished()) {
      val count = inflater.inflate(buffer)
      outputStream.write(buffer, 0, count)
    }
    outputStream.close()
    outputStream.toByteArray()
  }

  // def compress(text: String): Array[Byte] = {
  //   val baos = new ByteArrayOutputStream()
  //   try {
  //     val out = new DeflaterOutputStream(baos)
  //     out.write(text.getBytes("UTF-8"))
  //     out.close()
  //   } catch {
  //     case e: IOException => throw new AssertionError(e)
  //   }
  //   baos.toByteArray()
  // }


  // def decompress(bytes: Array[Byte]): String = {
  //   val in = new InflaterInputStream(new ByteArrayInputStream(bytes))
  //   val baos = new ByteArrayOutputStream()
  //   try {
  //     val buffer = new Array[Byte](8192)
  //     var len = in.read(buffer)
  //     while(len  > 0) {
  //       baos.write(buffer, 0, len);
  //       len = in.read(buffer)
  //     }
  //     new String(baos.toByteArray(), "UTF-8");
  //   } catch {
  //     case e: IOException => throw new AssertionError(e);
  //   }
  // }

  def bytesToAny(bytes: Array[Byte]): Any = {
    val in = new ObjectInputStream(new ByteArrayInputStream(bytes))
    in.readUnshared().asInstanceOf[Any]
  }

  /**
    * Translate a map of String and Any to an array of bytes
    *
    * @param o TreeMap object of type String and Any
    * @return an array of bytes representing the object binarized
    */
  def anyToBytes(o: Any): Array[Byte] = {
    val bos = new ByteArrayOutputStream()
    var out = new ObjectOutputStream(bos)
    out.writeObject(o);
    val bytes = bos.toByteArray()
    out.close();
    bos.close();
    bytes
  }

  /**
    * Translate arrays of bytes to Any Objects
    *
    * @param key represents a 16 bit key to generate the secret-key
    * @param inputFile a File from which this class reads the data
    * @return Object created by reading the array bytes from the file
    * @throws NoSuchAlgoritmException, NoSuchPaddingException, BadPaddingException,
    *         InvalidKeyException, IllegalBlockSizeException, IOException
    */
  @throws(classOf[NoSuchAlgorithmException])
  @throws(classOf[NoSuchPaddingException])
  @throws(classOf[BadPaddingException])
  @throws(classOf[InvalidKeyException])
  @throws(classOf[IllegalBlockSizeException])
  @throws(classOf[IOException])
  def readFromFile(key: String, inputFile: File): Any = {
    val cipherMode: Int = Cipher.DECRYPT_MODE
    val secretKey = new SecretKeySpec(key.getBytes(), Algorithm)
    val cipher = Cipher.getInstance(Transformation)
    cipher.init(cipherMode, secretKey)

    val inputStream = new FileInputStream(inputFile);
    val inputBytes = new Array[Byte](inputFile.length().toInt);
    inputStream.read(inputBytes);

    val outputBytes = cipher.doFinal(inputBytes)

    inputStream.close()

    val in = new ObjectInputStream(new ByteArrayInputStream(outputBytes))
    in.readObject()
  }

  def writeToFile(outputFile: File, outputBytes: Array[Byte], append: Boolean = false): Unit = {
    val outputStream = new FileOutputStream(outputFile, append)
    try {
      outputStream.write(outputBytes)
    } finally {
      outputStream.close()
    }
  }

  /**
    * Encryption method, currently uses AES algorithm
    *
    * @param key represents a 16 bit key to generate the secret-key
    * @param inputData a TreeMap[String, Any] representing a collection shard
    * @param outputFile the file that will be encrypted and persisted to disk
    * @return no return value
    * @throws NoSuchAlgorithmException, NoSuchPaddingException,
    * InvalidKeyException, BadPaddingException, IllegalBlockSizeException,
    * IOException
    */
  def encrypt(key: String, inputData: Any, outputFile: File, append: Boolean = false): Unit = {

    val cipherMode: Int = Cipher.ENCRYPT_MODE

    try {

      val secretKey = new SecretKeySpec(key.getBytes(), Algorithm)
      val cipher = Cipher.getInstance(Transformation)
      cipher.init(cipherMode, secretKey)

      val outputBytes = cipher.doFinal(anyToBytes(inputData))

      writeToFile(outputFile, outputBytes, append)

    } catch {
      case na: NoSuchAlgorithmException => println(s"Error encrypting/decrypting file $na")
      case np: NoSuchPaddingException => println(s"Error encrypting/decrypting file $np")
      case ik: InvalidKeyException => println(s"Error encrypting/decrypting file $ik")
      case bp: BadPaddingException => println(s"Error encrypting/decrypting file $bp")
      case ib: IllegalBlockSizeException => println(s"Error encrypting/decrypting file $ib")
      case io: IOException => println(s"Error encrypting/decrypting file $io")
    }

  }

  /**
    * Decryption method, currently uses AES algorithm
    *
    * @param key a 16 bit key used to decrypt the content of the encrypted file
    * @param inputFile a file read from filesystem
    * @return a TreeMap of type String and Any
    * @throws NoSuchAlgorithmException, NoSuchPaddingException,
    * InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException
    */
  def decrypt[T <: Any](key: String, inputFile: File): T = readFromFile(key, inputFile).asInstanceOf[T]

}

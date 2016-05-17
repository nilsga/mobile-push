package com.malliina.push

import java.io.StringWriter

import com.malliina.concurrent.ExecutionContexts.cached
import com.malliina.http.AsyncHttp
import org.asynchttpclient.Response

import scala.concurrent.Future
import scala.xml.{Elem, XML}

trait WindowsClient[T <: Token, M <: WindowsMessage] extends PushClient[T, M, Response] {
  override def pushAll(urls: Seq[T], message: M): Future[Seq[Response]] = {
    val bodyAsString = serialize(message.xml)
    sendMulti(urls, bodyAsString, message.headers)
  }

  protected def send(url: T, xml: Elem, headers: Map[String, String]): Future[Response] =
    sendSingle(url, serialize(xml), headers)

  protected def sendMulti(urls: Seq[T], body: String, headers: Map[String, String]) =
    Future.sequence(urls.map(url => sendSingle(url, body, headers)))

  protected def sendSingle(url: T, body: String, headers: Map[String, String]) =
    AsyncHttp.post(url.token, body, headers)

  /** Serializes `elem` to a string, adding an xml declaration to the top. Encodes the payload
    * automatically as described in
    * http://msdn.microsoft.com/en-us/library/windowsphone/develop/hh202945(v=vs.105).aspx.
    *
    * @param elem xml
    * @return xml as a string
    */
  protected def serialize(elem: Elem): String = {
    val writer = new StringWriter
    // xmlDecl = true prepends this as the first line, as desired: <?xml version="1.0" encoding="utf-8"?>
    XML.write(writer, elem, "UTF-8", xmlDecl = true, doctype = null)
    writer.toString
  }
}

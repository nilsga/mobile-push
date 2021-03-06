package com.malliina.push.apns

import java.security.cert.X509Certificate
import javax.net.ssl.{SSLSocketFactory, X509TrustManager}

import com.malliina.http.OkClient
import okhttp3.{OkHttpClient, Protocol}

import scala.collection.JavaConverters.seqAsJavaList

object APNSCertClient {
  val tm: X509TrustManager = new X509TrustManager {
    override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = ()

    override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = ()

    override def getAcceptedIssuers: Array[X509Certificate] = Array.empty[X509Certificate]
  }

  def httpClient(ssf: SSLSocketFactory): OkHttpClient = {
    new OkHttpClient.Builder()
      .sslSocketFactory(ssf, tm)
      .protocols(seqAsJavaList(List(Protocol.HTTP_2, Protocol.HTTP_1_1)))
      .build()
  }
}

class APNSCertClient(socketFactory: SSLSocketFactory, isSandbox: Boolean = false)
  extends APNSHttpClient(OkClient.ssl(socketFactory, APNSCertClient.tm), isSandbox)

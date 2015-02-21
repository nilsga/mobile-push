package tests

import java.io.FileInputStream
import java.nio.file.{Path, Paths}
import java.security.KeyStore

import com.mle.file.{FileUtilities, StorageFile}
import com.mle.push.apns._
import com.mle.security.KeyStores
import com.mle.util.{BaseConfigReader, Util}
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

/**
 * @author Michael
 */
class APNSTests extends FunSuite {
  test("certificate is valid") {
    val creds = APNSCreds.load
    KeyStores.validateKeyStore(creds.file, creds.pass, "PKCS12")
  }

  test("can send") {
    val creds = APNSCreds.load
    val ks = keyStoreFromFile(creds.file, creds.pass, "PKCS12").get
    val client = new APNSClient(ks, creds.pass, isSandbox = true)
    //    val message = APNSMessage.badged("I <3 U!", 3)
    val payload = AlertPayload(
      "this is a body",
      title = Some("hey"),
      actionLocKey = Some("POMP"),
      locKey = Some("MSG_FORMAT"),
      locArgs = Some(Seq("Emilia", "Jaana")))
    val advancedMessage = APNSMessage(APSPayload(Right(payload), sound = Some("default")))
    val deviceID = "9f3c2f830256954ada78bf56894fa7586307f0eedb7763117c84e0c1eee8347a"
    val fut = client.push(deviceID, advancedMessage)
    val notification = Await.result(fut, 5.seconds)
  }

  def keyStoreFromFile(file: Path, pass: String, storeType: String = "JKS"): Try[KeyStore] = Try {
    val ks = KeyStore.getInstance(storeType)
    Util.using(new FileInputStream(file.toFile))(keyStream => {
      ks.load(keyStream, pass.toCharArray)
      ks
    })
  }
}

case class APNSCred(file: Path, pass: String)

object APNSCreds extends BaseConfigReader[APNSCred] {
  override def userHomeConfPath: Path = FileUtilities.userHome / "keys" / "aps.conf"

  override def resourceCredential: String = ""

  override def loadOpt: Option[APNSCred] = fromUserHomeOpt

  override def fromMapOpt(map: Map[String, String]): Option[APNSCred] = for {
    file <- map get "aps_file"
    pass <- map get "aps_pass"
  } yield APNSCred(Paths get file, pass)
}
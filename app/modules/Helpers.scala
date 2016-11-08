package modules

import java.security.spec.PKCS8EncodedKeySpec
import java.security.KeyFactory
import javax.crypto.Cipher
import javax.inject.Inject

import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.ReadPreference
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}


class Helpers @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext) {

  def keysCollectionFuture: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("keys"))

  def decryptUserPassword(encryptedPassword: String): Future[String] = {
    for {
      keysCollection <- keysCollectionFuture
      result <- {
        import play.modules.reactivemongo.json.ImplicitBSONHandlers._
        keysCollection.find(Json.obj("id" -> 1), Json.obj("privateKey" -> 1)).one[JsObject](ReadPreference.primary).map {
          case None => "Failed"
          case Some(privateKey: JsObject) => {
            val factory = KeyFactory.getInstance("RSA")

            val cipher = Cipher.getInstance("RSA")

//            val keyBytes = Files.readAllBytes(new File("private_key.der").toPath)

            val keyBytes = (privateKey \ "privateKey").as[Array[Byte]]

            cipher.init(Cipher.DECRYPT_MODE, factory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes)))

            new String(cipher.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(encryptedPassword)))

          }
        }
      }
    } yield {
      result
    }
  }

}

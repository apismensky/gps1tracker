package modules

import java.io.File
import java.nio.file.Files
import java.security.spec.PKCS8EncodedKeySpec
import java.security.KeyFactory
import javax.crypto.Cipher


class Helpers {

  def decryptUserPassword(encryptedPassword: String): String = {
    val factory = KeyFactory.getInstance("RSA")

    val cipher = Cipher.getInstance("RSA")

    val keyBytes = Files.readAllBytes(new File("private_key.der").toPath)

    cipher.init(Cipher.DECRYPT_MODE, factory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes)))

    new String(cipher.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(encryptedPassword)))
  }

}

package org.kotlink.framework.crypto

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesEncryptionProvider(
    encryptionKey: String
) : EncryptionProvider {
    private val secureRandom = SecureRandom()
    private val secretKeySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
    private val cachedCipher = object : ThreadLocal<Cipher>() {
        override fun initialValue(): Cipher {
            return Cipher.getInstance("AES/CBC/PKCS5Padding")
        }
    }

    override fun encrypt(text: String): String {
        val cipher = cachedCipher.get()
        val initialVector = ByteArray(cipher.blockSize).apply { secureRandom.nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(initialVector))
        val cipherBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        val cipherBytesWithIv = ByteArray(initialVector.size + cipherBytes.size).apply {
            System.arraycopy(initialVector, 0, this, 0, initialVector.size)
            System.arraycopy(cipherBytes, 0, this, initialVector.size, cipherBytes.size)
        }
        return Base64.getEncoder().encodeToString(cipherBytesWithIv)
    }

    override fun decrypt(text: String): String {
        val decodedBytes = Base64.getDecoder().decode(text)
        val cipher = cachedCipher.get()
        val cipherBytes = decodedBytes.copyOfRange(cipher.blockSize, decodedBytes.size)
        val ivParameterSpec = IvParameterSpec(decodedBytes, 0, cipher.blockSize)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        return String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
    }
}
package org.kotlink.framework.crypto

interface EncryptionProvider {
    fun encrypt(text: String): String
    fun decrypt(text: String): String
}
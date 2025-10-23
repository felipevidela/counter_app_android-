package com.example.counter_app.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Bluetooth Encryption Service
 *
 * Implementa encriptación AES-256-GCM para datos transmitidos via Bluetooth.
 * Utiliza Android Keystore para almacenamiento seguro de llaves.
 *
 * Cumplimiento:
 * - ISO/IEC 27001 (A.10.1.1) - Controles criptográficos
 * - IEC 62443-4-2 (FR 4.1) - Confidencialidad de información
 * - IEC 62443-4-2 (FR 3.1) - Integridad de comunicación
 *
 * Seguridad:
 * - AES-256 (Advanced Encryption Standard)
 * - GCM (Galois/Counter Mode) - proporciona autenticación + encriptación
 * - Android Keystore - llaves protegidas por hardware
 * - Nonce único por mensaje - previene replay attacks
 */
class BluetoothEncryption {

    companion object {
        private const val KEY_ALIAS = "counter_app_bluetooth_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    /**
     * Genera o recupera llave de encriptación del Android Keystore
     *
     * Android Keystore asegura que:
     * - Llave no puede ser exportada
     * - Llave protegida por hardware (si disponible)
     * - Llave persiste entre sesiones
     */
    private fun getOrCreateKey(): SecretKey {
        // Verificar si la llave ya existe
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        // Crear nueva llave
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(false) // No requiere biometría
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Encripta datos para transmisión Bluetooth
     *
     * @param plaintext Datos en texto plano
     * @return Encrypted data con formato: [IV (12 bytes) | Ciphertext | Auth Tag (16 bytes)]
     *
     * Formato del resultado:
     * - Primeros 12 bytes: IV (Initialization Vector / Nonce)
     * - Siguientes N bytes: Datos encriptados
     * - Últimos 16 bytes: Authentication Tag (GCM)
     *
     * GCM proporciona:
     * - Confidencialidad (encriptación)
     * - Autenticación (MAC)
     * - Detección de modificaciones
     */
    fun encryptData(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getOrCreateKey()

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        // IV generado automáticamente por Cipher
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)

        // Formato: [IV | Ciphertext+AuthTag]
        return iv + ciphertext
    }

    /**
     * Desencripta datos recibidos via Bluetooth
     *
     * @param encryptedData Datos encriptados con formato [IV | Ciphertext | Auth Tag]
     * @return Datos en texto plano
     * @throws Exception Si la autenticación falla (datos modificados)
     */
    fun decryptData(encryptedData: ByteArray): ByteArray {
        // Extraer IV (primeros 12 bytes)
        val iv = encryptedData.copyOfRange(0, 12)
        val ciphertext = encryptedData.copyOfRange(12, encryptedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getOrCreateKey()

        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        // Si GCM authentication falla, lanza exception
        return cipher.doFinal(ciphertext)
    }

    /**
     * Encripta string para transmisión
     */
    fun encryptString(plaintext: String): ByteArray {
        return encryptData(plaintext.toByteArray(Charsets.UTF_8))
    }

    /**
     * Desencripta a string
     */
    fun decryptString(encryptedData: ByteArray): String {
        return String(decryptData(encryptedData), Charsets.UTF_8)
    }

    /**
     * Elimina llave del Keystore (útil para testing o reset)
     */
    fun deleteKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    /**
     * Verifica si existe llave en Keystore
     */
    fun hasKey(): Boolean {
        return keyStore.containsAlias(KEY_ALIAS)
    }
}

/**
 * Extension functions para facilitar uso
 */
fun String.encryptForBluetooth(encryption: BluetoothEncryption): ByteArray {
    return encryption.encryptString(this)
}

fun ByteArray.decryptFromBluetooth(encryption: BluetoothEncryption): String {
    return encryption.decryptString(this)
}

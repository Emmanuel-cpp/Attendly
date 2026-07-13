package com.siamoonga.attendance.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object QrCodeUtil {

    const val SLOT_SECONDS = 20L

    fun generateSecret(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun currentSlot(): Long = System.currentTimeMillis() / 1000 / SLOT_SECONDS

    fun secondsLeftInSlot(): Long {
        val now = System.currentTimeMillis() / 1000
        return SLOT_SECONDS - (now % SLOT_SECONDS)
    }

    fun tokenFor(secret: String, slot: Long): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        val raw = mac.doFinal(slot.toString().toByteArray())
        return raw.joinToString("") { "%02x".format(it) }.take(16)
    }

    fun buildPayload(sessionId: String, secret: String): String =
        "$sessionId|${tokenFor(secret, currentSlot())}"

    fun toQrBitmap(content: String, size: Int = 800): Bitmap {
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
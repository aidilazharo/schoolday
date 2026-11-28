package com.schoolday.app

data class Envelope(val version: Int, val id: String, val senderId: String, val recipientId: String, val ciphertext: String, val expiresAt: Long) {
    fun isValid(now: Long): Boolean =
        version == 0 && id.isNotEmpty() && senderId.isNotEmpty() && recipientId.isNotEmpty() &&
        ciphertext.isNotEmpty() && expiresAt in 1L..9007199254740991L && expiresAt > now
}

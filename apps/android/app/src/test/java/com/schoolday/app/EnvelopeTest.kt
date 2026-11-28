package com.schoolday.app

import org.junit.Assert.*
import org.junit.Test

class EnvelopeTest {
    @Test fun contract() {
        val valid = Envelope(0, "m1", "a", "b", "fixture-only", 2000)
        assertTrue(valid.isValid(1000))
        assertFalse(valid.copy(version = 1).isValid(1000))
        assertFalse(valid.copy(expiresAt = 1000).isValid(1000))
        assertFalse(valid.copy(senderId = "").isValid(1000))
    }
}

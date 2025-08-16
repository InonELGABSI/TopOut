package com.topout.kmp.utils

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

class GenerateIdTest {

    @Test
    fun generateId_returnsNonEmptyString() {
        val id = generateId()
        assertTrue(id.isNotBlank(), "Generated ID should not be blank")
        assertTrue(id.length >= 8, "Generated ID should have reasonable length")
        assertTrue(id.contains('-'), "Generated ID should contain hyphens like a UUID/NSUUID")
    }

    @Test
    fun generateId_multipleCallsProduceDifferentValues() {
        val ids = (1..5).map { generateId() }
        ids.forEach { id ->
            assertTrue(id.isNotBlank())
            assertTrue(id.contains('-'))
        }
        // Ensure at least two are different (extremely high probability all are unique)
        assertNotEquals(ids.first(), ids[1], "Two subsequent generated IDs should differ")
    }
}


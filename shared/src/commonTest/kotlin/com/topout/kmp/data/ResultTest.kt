package com.topout.kmp.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private data class TestError(override val message: String): Error

class ResultTest {

    @Test
    fun success_containsData() {
        val r: Result<Int, TestError> = Result.Success(42)
        assertTrue(r is Result.Success)
        assertEquals(42, r.data)
    }

    @Test
    fun success_allowsNullData() {
        val r: Result<String, TestError> = Result.Success(null)
        assertTrue(r is Result.Success)
        assertEquals(null, r.data)
    }

    @Test
    fun failure_containsError() {
        val err = TestError("boom")
        val r: Result<Int, TestError> = Result.Failure(err)
        assertTrue(r is Result.Failure)
        assertEquals("boom", r.error?.message)
    }

    @Test
    fun failure_allowsNullError() {
        val r: Result<Int, TestError> = Result.Failure(null)
        assertTrue(r is Result.Failure)
        assertEquals(null, r.error)
    }
}


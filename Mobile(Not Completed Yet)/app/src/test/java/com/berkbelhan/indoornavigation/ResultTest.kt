package com.berkbelhan.indoornavigation

import com.berkbelhan.indoornavigation.core.common.AppError
import com.berkbelhan.indoornavigation.core.common.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    @Test
    fun `success result holds value`() {
        val result: Result<Int> = Result.Success(42)
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `failure result holds error`() {
        val result: Result<Int> = Result.Failure(AppError.Network("timeout"))
        assertTrue(result.isFailure)
        assertEquals(null, result.getOrNull())
    }

    @Test
    fun `map transforms success value`() {
        val result = Result.Success(10).map { it * 2 }
        assertEquals(20, result.getOrNull())
    }

    @Test
    fun `map propagates failure`() {
        val result: Result<Int> = Result.Failure(AppError.Auth("not authenticated"))
        val mapped = result.map { it * 2 }
        assertTrue(mapped.isFailure)
    }
}

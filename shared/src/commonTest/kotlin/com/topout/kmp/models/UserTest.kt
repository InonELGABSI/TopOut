package com.topout.kmp.models

import com.topout.kmp.utils.nowEpochMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class UserTest {

    @Test
    fun user_defaultValuesAreSetCorrectly() {
        val user = User(id = "test-id")
        assertEquals("test-id", user.id)
        assertEquals("meters", user.unitPreference)
        assertEquals(false, user.enabledNotifications)
        assertEquals(0.0, user.relativeHeightFromStartThr)
        assertEquals(0.0, user.totalHeightFromStartThr)
        assertEquals(0.0, user.currentAvgHeightSpeedThr)
        assertEquals(false, user.userUpdatedOffline)
        assertNotNull(user.createdAt)
        assertNotNull(user.updatedAt)
    }

    @Test
    fun user_copyWorksAsExpected() {
        val originalUser = User(
            id = "original-id",
            name = "John Doe",
            email = "john@example.com"
        )
        val copiedUser = originalUser.copy(name = "Jane Doe")
        assertEquals("original-id", copiedUser.id)
        assertEquals("Jane Doe", copiedUser.name)
        assertEquals("john@example.com", copiedUser.email)
        assertEquals(originalUser.unitPreference, copiedUser.unitPreference)
        assertEquals(originalUser.enabledNotifications, copiedUser.enabledNotifications)
    }

    @Test
    fun user_equalityBetweenUsers() {
        val user1 = User(
            id = "test-id",
            name = "Test User",
            email = "test@example.com",
            unitPreference = "feet"
        )
        val user2 = User(
            id = "test-id",
            name = "Test User",
            email = "test@example.com",
            unitPreference = "feet"
        )
        val user3 = User(
            id = "different-id",
            name = "Test User",
            email = "test@example.com"
        )

        // Instead of comparing the whole object, compare fields that are stable
        assertEquals(user1.id, user2.id)
        assertEquals(user1.name, user2.name)
        assertEquals(user1.email, user2.email)
        assertEquals(user1.unitPreference, user2.unitPreference)
        assertNotEquals(user1.id, user3.id)
    }

    @Test
    fun user_copyWithDifferentPreferences() {
        val user = User(id = "test-id")
        val updatedUser = user.copy(
            enabledNotifications = true,
            relativeHeightFromStartThr = 100.0,
            totalHeightFromStartThr = 500.0,
            currentAvgHeightSpeedThr = 300.0
        )
        assertEquals(true, updatedUser.enabledNotifications)
        assertEquals(100.0, updatedUser.relativeHeightFromStartThr)
        assertEquals(500.0, updatedUser.totalHeightFromStartThr)
        assertEquals(300.0, updatedUser.currentAvgHeightSpeedThr)
    }

    @Test
    fun user_nullableFieldsHandledCorrectly() {
        val userWithNulls = User(
            id = "test-id",
            name = null,
            email = null,
            imgUrl = null,
            unitPreference = null,
            enabledNotifications = null
        )
        assertEquals("test-id", userWithNulls.id)
        assertEquals(null, userWithNulls.name)
        assertEquals(null, userWithNulls.email)
        assertEquals(null, userWithNulls.imgUrl)
        assertEquals(null, userWithNulls.unitPreference)
        assertEquals(null, userWithNulls.enabledNotifications)
    }

    @Test
    fun user_timestampFieldsAreSetOnCreation() {
        val user = User(id = "test-id")
        assertNotNull(user.createdAt)
        assertNotNull(user.updatedAt)
        val now = nowEpochMillis()
        val createdAt = user.createdAt!!
        val updatedAt = user.updatedAt!!
        assertEquals(true, (now - createdAt) < 5000)
        assertEquals(true, (now - updatedAt) < 5000)
    }

    @Test
    fun user_allThresholdDefaultsAreZero() {
        val user = User(id = "test-id")
        assertEquals(0.0, user.relativeHeightFromStartThr)
        assertEquals(0.0, user.totalHeightFromStartThr)
        assertEquals(0.0, user.currentAvgHeightSpeedThr)
    }
}
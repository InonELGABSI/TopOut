package com.topout.kmp.data.dao

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import com.topout.kmp.AppDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
actual class DatabaseDriverFactory(
    private val context: Context
) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver (
            schema = AppDatabase.Schema,
            context = context,
            name = "sessions.db"
        )
    }
}
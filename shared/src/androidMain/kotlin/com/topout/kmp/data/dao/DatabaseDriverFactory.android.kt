package com.topout.kmp.data.dao

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import com.topout.kmp.AppDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(
    private val context: Context
) {
    actual fun createDriver(): SqlDriver {
        // Force the app to recreate the database by using a new version name
        // This will handle the schema mismatch by creating a fresh database
        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
            name = "topout_db_v3.db",
        )
    }
}

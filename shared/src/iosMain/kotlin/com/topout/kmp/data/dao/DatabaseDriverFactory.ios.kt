package com.topout.kmp.data.dao

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.topout.kmp.AppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            AppDatabase.Schema,
            name= "topout_db_v1.db")
    }
}


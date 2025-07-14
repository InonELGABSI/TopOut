package com.topout.kmp.data.user

import com.topout.kmp.data.Result
import com.topout.kmp.models.User

interface UserRepository {
    suspend fun getUser():Result<User, UserError>
}
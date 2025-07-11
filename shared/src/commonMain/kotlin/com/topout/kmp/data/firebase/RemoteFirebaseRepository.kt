package com.topout.kmp.data.firebase

import com.topout.kmp.data.user.UserError
import com.topout.kmp.models.Session
import com.topout.kmp.models.Sessions
import com.topout.kmp.models.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import com.topout.kmp.data.Result
import com.topout.kmp.data.sessions.SessionsError
import io.ktor.util.date.getTimeMillis


class RemoteFirebaseRepository : FirebaseRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val sessionsCollection = firestore.collection("sessions")
    private val usersCollection = firestore.collection("users")

    override suspend fun getSessions(): Result<List<Session>, SessionsError> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.Failure(SessionsError("User not authenticated"))

            val sessions = sessionsCollection
                .where { "userId" equalTo uid }
                .get()
                .documents
                .map { it.data<Session>() }

            Result.Success(sessions)
        } catch (e: Exception) {
            Result.Failure(SessionsError(e.message ?: "Failed to get sessions"))
        }
    }

    override suspend fun saveSession(session: Session) {
        val uid = auth.currentUser?.uid ?: return
        val sessionWithUserId = session.id?.let {
            sessionsCollection.document(it.toString())
                .set(session.copy(userId = uid))
        } ?: sessionsCollection.add(session.copy(userId = uid))
    }

    override suspend fun updateSession(session: Session) {
        session.id?.let {
            sessionsCollection.document(it.toString())
                .set(session)
        }
    }

    override suspend fun deleteSession(sessionId: String) {
        sessionsCollection.document(sessionId).delete()
    }

    override suspend fun signInAnonymously(): Result<User, UserError> = try {
        val firebaseUser = auth.currentUser ?: auth.signInAnonymously().user
        firebaseUser?.let {
            Result.Success(User(id = it.uid))
        } ?: Result.Failure(UserError("Failed to get Firebase user"))
    } catch (e: Exception) {
        Result.Failure(UserError(e.message ?: "Unknown error"))
    }


    override suspend fun getUser(): Result<User, UserError> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.Failure(UserError("User not authenticated"))

            val user = usersCollection
                .document(uid)
                .get()
                .data<User>()

            user.let { Result.Success(it) }
        } catch (e: Exception) {
            Result.Failure(UserError(e.message ?: "Unknown error"))
        }
    }


    override suspend fun ensureUserDocument(): Result<Unit, UserError> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.Failure(UserError("User not authenticated"))

            val docRef   = usersCollection.document(uid)
            val snapshot = docRef.get()

            if (!snapshot.exists) {
                val dataToSave = User(id = uid)
                docRef.set(dataToSave, merge = true)
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(UserError(e.message ?: "Failed to ensure user document"))
        }
    }




}

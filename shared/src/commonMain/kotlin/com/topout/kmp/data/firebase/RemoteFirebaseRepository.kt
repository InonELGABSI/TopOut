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
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.utils.extensions.asSessionTitle
import com.topout.kmp.utils.extensions.toFirestoreMap
import com.topout.kmp.utils.extensions.toSession
import dev.gitlive.firebase.firestore.DocumentSnapshot
import io.ktor.util.date.getTimeMillis
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Clock


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
                .map { it.toSession() }

            Result.Success(sessions)
        } catch (e: Exception) {
            Result.Failure(SessionsError(e.message ?: "Failed to get sessions"))
        }
    }

    override suspend fun saveSession(session: Session) {
        val uid = auth.currentUser?.uid ?: return
        val map = session.copy(userId = uid)
            .toFirestoreMap(serverCreatedAt = true)  // server time for created_at
        sessionsCollection.document(session.id).set(map)
    }

    override suspend fun updateSession(session: Session) {
        session.id.let {
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

    override suspend fun createSession(session: Session): Result<Session, SessionsError> {
        val uid = auth.currentUser?.uid
            ?: return Result.Failure(SessionsError("User not authenticated"))

        return try {
            val now = Clock.System.now()

            val updatedSession = session.copy(
                userId = uid,
                title = now.asSessionTitle()
            )

            // write with the deterministic id generated in Session()
            sessionsCollection.document(updatedSession.id).set(updatedSession)

            Result.Success(updatedSession)
        } catch (e: Exception) {
            Result.Failure(SessionsError(e.message ?: "Failed to create session"))
        }
    }

    override suspend fun pushTrackPoints(sessionId: String, points: List<TrackPoint>) {
        if (points.isEmpty()) return

        val batch = firestore.batch()                                   // ✔ create batch
        val sub   = sessionsCollection
            .document(sessionId)
            .collection("trackPoints")

        points.forEach { p ->
            batch.set(sub.document(p.id.toString()), p.toFirestoreMap()) // ✔ enqueue
        }
        batch.commit()                                                   // ✔ commit
    }

}

package com.topout.kmp.data.firebase

import com.topout.kmp.data.user.UserError
import com.topout.kmp.models.Session
import com.topout.kmp.models.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import com.topout.kmp.data.Result
import com.topout.kmp.data.Error
import com.topout.kmp.data.sessions.SessionsError
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.utils.extensions.asSessionTitle
import com.topout.kmp.utils.extensions.toFirestoreMap
import com.topout.kmp.utils.extensions.toSession
import com.topout.kmp.utils.extensions.toTrackPoint
import kotlinx.datetime.Clock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Helper extension to wrap Firebase operations with timeout and consistent error handling
 */
private suspend inline fun <T, E : Error> withFirebaseTimeout(
    timeoutMs: Long = 5_000,
    crossinline errorFactory: (String) -> E,
    crossinline operation: suspend () -> T
): Result<T, E> {
    return try {
        val result = withTimeout(timeoutMs) {
            operation()
        }
        Result.Success(result)
    } catch (e: TimeoutCancellationException) {
        Result.Failure(errorFactory("Request timed out - check your internet connection"))
    } catch (e: Exception) {
        Result.Failure(errorFactory(e.message ?: "Unknown error occurred"))
    }
}

class RemoteFirebaseRepository : FirebaseRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val sessionsCollection = firestore.collection("sessions")
    private val usersCollection = firestore.collection("users")

    override suspend fun getSessions(): Result<List<Session>, SessionsError> {
        val uid = auth.currentUser?.uid
            ?: return Result.Failure(SessionsError("User not authenticated"))

        return withFirebaseTimeout(
            errorFactory = ::SessionsError
        ) {
            sessionsCollection
                .where { "user_id" equalTo uid }
                .get()
                .documents
                .map { it.toSession() }
        }
    }

    override suspend fun getSessionsUpdatedAfter(timestamp: Long): Result<List<Session>, SessionsError> {
        val uid = auth.currentUser?.uid
            ?: return Result.Failure(SessionsError("User not authenticated"))

        return withFirebaseTimeout(
            errorFactory = ::SessionsError
        ) {
            sessionsCollection
                .where {
                    "user_id" equalTo uid
                    "updated_at" greaterThan timestamp
                }
                .get()
                .documents
                .map { it.toSession() }
        }
    }

    override suspend fun saveSession(session: Session): Result<Session, SessionsError> {
        val uid = auth.currentUser?.uid
            ?: return Result.Failure(SessionsError("User not authenticated"))

        val now = Clock.System.now()

        val updatedSession = session.copy(
            userId = uid,
        )

        val map = updatedSession.toFirestoreMap()

        return withFirebaseTimeout(
            errorFactory = ::SessionsError
        ) {
            sessionsCollection.document(session.id).set(map)
            updatedSession // Return the updated session
        }
    }

    override suspend fun updateSession(session: Session): Result<Unit, SessionsError> {
        return withFirebaseTimeout(
            errorFactory = ::SessionsError
        ) {
            sessionsCollection.document(session.id).set(session.toFirestoreMap())
        }
    }

    override suspend fun deleteSession(sessionId: String): Result<Unit, SessionsError> {
        return withFirebaseTimeout(
            errorFactory = ::SessionsError
        ) {
            sessionsCollection.document(sessionId).delete()
        }
    }

    override suspend fun signInAnonymously(): Result<User, UserError> {
        return withFirebaseTimeout(
            errorFactory = ::UserError
        ) {
            val firebaseUser = auth.currentUser ?: auth.signInAnonymously().user
            firebaseUser?.let {
                User(id = it.uid)
            } ?: throw Exception("Failed to get Firebase user")
        }
    }

    override suspend fun getUser(): Result<User, UserError> {
        val uid = auth.currentUser?.uid
            ?: return Result.Failure(UserError("User not authenticated"))

        return withFirebaseTimeout(
            errorFactory = ::UserError
        ) {
            usersCollection
                .document(uid)
                .get()
                .data<User>()
        }
    }

    override suspend fun updateUser(user: User): Result<User, UserError> {
        val uid = auth.currentUser?.uid
            ?: return Result.Failure(UserError("User not authenticated"))

        return withFirebaseTimeout(
            errorFactory = ::UserError
        ) {
            usersCollection
                .document(uid)
                .set(user.toFirestoreMap(), merge = true)
            user
        }
    }

    override suspend fun ensureUserDocument(): Result<Unit, UserError> {
        val uid = auth.currentUser?.uid
            ?: return Result.Failure(UserError("User not authenticated"))

        return withFirebaseTimeout(
            errorFactory = ::UserError
        ) {
            val docRef = usersCollection.document(uid)
            val snapshot = docRef.get()

            if (!snapshot.exists) {
                val dataToSave = User(id = uid)
                docRef.set(dataToSave, merge = true)
            }
        }
    }

    override suspend fun pushTrackPoints(sessionId: String, points: List<TrackPoint>): Result<Unit, SessionsError> {
        if (points.isEmpty()) return Result.Success(Unit)

        return withFirebaseTimeout(
            errorFactory = ::SessionsError
        ) {
            val batch = firestore.batch()
            val sub = sessionsCollection
                .document(sessionId)
                .collection("trackPoints")

            points.forEach { p ->
                batch.set(sub.document(p.id.toString()), p.toFirestoreMap())
            }
            batch.commit()
        }
    }

    override suspend fun getSessionById(sessionId: String): Result<Session?, SessionsError> {
        return withFirebaseTimeout(
            errorFactory = ::SessionsError
        ) {
            val doc = sessionsCollection.document(sessionId).get()
            if (doc.exists) {
                doc.toSession()
            } else {
                null
            }
        }
    }

    override suspend fun getTrackPointsBySession(sessionId: String): Result<List<TrackPoint>, SessionsError> {
        return withFirebaseTimeout(
            errorFactory = ::SessionsError
        ) {
            sessionsCollection
                .document(sessionId)
                .collection("trackPoints")
                .get()
                .documents
                .map { it.toTrackPoint() }
        }
    }
}

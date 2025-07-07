package com.topout.kmp.data.firebase

import com.topout.kmp.models.Session
import com.topout.kmp.models.Sessions
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

class RemoteFirebaseRepository : FirebaseRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    override suspend fun getSessions(): List<Session> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("users")
            .document(uid)
            .collection("sessions")
            .get()
            .documents
            .map {it.data()}
    }


    override suspend fun saveSession(session: Session) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(uid)
            .collection("sessions")
            .document(session.id.toString())
            .set(session)
    }
    override suspend fun updateSession(session: Session) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(uid)
            .collection("sessions")
            .document(session.id.toString())
            .set(session)
    }
    override suspend fun deleteSession(sessionId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(uid)
            .collection("sessions")
            .document(sessionId)
            .delete()
    }
    override suspend fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
        }
    }
}

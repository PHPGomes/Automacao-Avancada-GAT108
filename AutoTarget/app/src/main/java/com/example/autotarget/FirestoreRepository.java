package com.example.autotarget;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreRepository {

    private FirebaseFirestore db;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public void saveGameResult(String userId, Map<String, Object> gameData, FirestoreCallback<Void> callback) {
        db.collection("users").document(userId)
                .collection("partidas")
                .add(gameData)
                .addOnSuccessListener(documentReference -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void getRanking(FirestoreCallback<List<Map<String, Object>>> callback) {
        db.collectionGroup("partidas")
                .orderBy("pontuacaoFinal", Query.Direction.DESCENDING)
                .limit(10) // Top 10 ranking
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> ranking = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ranking.add(document.getData());
                        }
                        callback.onSuccess(ranking);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void getTelemetryData(String userId, FirestoreCallback<List<Map<String, Object>>> callback) {
        db.collection("users").document(userId)
                .collection("telemetria")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100) // Get last 100 telemetry entries
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> telemetry = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            telemetry.add(document.getData());
                        }
                        callback.onSuccess(telemetry);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void saveTelemetryData(String userId, Map<String, Object> telemetryData, FirestoreCallback<Void> callback) {
        db.collection("users").document(userId)
                .collection("telemetria")
                .add(telemetryData)
                .addOnSuccessListener(documentReference -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}

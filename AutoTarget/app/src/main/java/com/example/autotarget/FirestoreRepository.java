package com.example.autotarget;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
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


    // SALVAR PARTIDA
    public void saveGameResult(String userId,
                               Map<String, Object> gameData,
                               FirestoreCallback<Void> callback) {

        Log.d("FIRESTORE", "SALVANDO PARTIDA");

        db.collection("users")
                .document(userId)
                .collection("partidas")
                .add(gameData)
                .addOnSuccessListener(documentReference -> {

                    Log.d("FIRESTORE", "PARTIDA SALVA NO USUARIO");

                    db.collection("ranking")
                            .add(gameData)
                            .addOnSuccessListener(rankingDoc -> {

                                Log.d("FIRESTORE", "RANKING SALVO");

                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {

                                Log.e("FIRESTORE", "ERRO RANKING", e);

                                callback.onFailure(e);
                            });

                })
                .addOnFailureListener(e -> {

                    Log.e("FIRESTORE", "ERRO PARTIDA", e);

                    callback.onFailure(e);
                });
    }



    // RANKING GLOBAL
    public void getRanking(FirestoreCallback<List<Map<String,Object>>> callback) {

       // Log.d("RANK", "BUSCANDO RANKING");

        db.collection("ranking")
                .orderBy("finalScore", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(result -> {

                    //Log.d("RANK", "DOCUMENTOS: " + result.size());

                    List<Map<String,Object>> ranking = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : result) {

                       // Log.d("RANK", doc.getData().toString());

                        ranking.add(doc.getData());
                    }

                    callback.onSuccess(ranking);

                })
                .addOnFailureListener(e -> {

                    Log.e("RANK", "ERRO RANK", e);
                    callback.onFailure(e);

                });

    }




    // TELEMETRIA
    public void getTelemetryData(String userId,
                                 FirestoreCallback<List<Map<String,Object>>> callback){


        db.collection("users")
                .document(userId)
                .collection("telemetria")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {


                    List<Map<String,Object>> telemetry = new ArrayList<>();

                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){

                        telemetry.add(doc.getData());

                    }

                    callback.onSuccess(telemetry);


                })
                .addOnFailureListener(callback::onFailure);
    }





    // SALVAR TELEMETRIA
    public void saveTelemetryData(String userId,
                                  Map<String,Object> telemetryData,
                                  FirestoreCallback<Void> callback){


        db.collection("users")
                .document(userId)
                .collection("telemetria")
                .add(telemetryData)
                .addOnSuccessListener(documentReference ->
                        callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);

    }

}
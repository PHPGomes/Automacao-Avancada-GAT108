package com.example.autotarget;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankingActivity extends AppCompatActivity {

    private ListView rankingListView;
    private FirestoreRepository firestoreRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        rankingListView = findViewById(R.id.rankingListView);
        firestoreRepository = new FirestoreRepository();

        loadRanking();
    }

    private void loadRanking() {
        firestoreRepository.getRanking(new FirestoreRepository.FirestoreCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> rankingList) {
                List<String> displayList = new ArrayList<>();
                for (Map<String, Object> entry : rankingList) {
                    try {
                        String playerName = Cryptography.decrypt((String) entry.get("playerName"));
                        String finalScore = Cryptography.decrypt((String) entry.get("finalScore"));
                        displayList.add(playerName + " - " + finalScore + " pontos");
                    } catch (Exception e) {
                        Log.e("RankingActivity", "Erro ao descriptografar dados: " + e.getMessage());
                        displayList.add("Erro ao carregar dados");
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(RankingActivity.this, android.R.layout.simple_list_item_1, displayList);
                rankingListView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RankingActivity.this, "Erro ao carregar ranking: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("RankingActivity", "Erro ao carregar ranking", e);
            }
        });
    }
}

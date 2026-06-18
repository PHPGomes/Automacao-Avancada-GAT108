package com.example.autotarget;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        gameView = findViewById(R.id.gameView);
        Button btnIniciar = findViewById(R.id.btnIniciar);
        Button btnAdicionarEsquerda = findViewById(R.id.btnAdicionarEsquerda);
        Button btnAdicionarDireita = findViewById(R.id.btnAdicionarDireita);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRanking = findViewById(R.id.btnRanking);

        btnIniciar.setOnClickListener(v -> {
            if (currentUser != null) {
                gameView.iniciarJogo();
            } else {
                Toast.makeText(MainActivity.this, "Faça login para iniciar o jogo!", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdicionarEsquerda.setOnClickListener(v -> {
            if (currentUser != null) {
                gameView.adicionarCanhao(Lado.ESQUERDO);
            } else {
                Toast.makeText(MainActivity.this, "Faça login para adicionar canhões!", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdicionarDireita.setOnClickListener(v -> {
            if (currentUser != null) {
                gameView.adicionarCanhao(Lado.DIREITO);
            } else {
                Toast.makeText(MainActivity.this, "Faça login para adicionar canhões!", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            // Implementar lógica de login/registro aqui
            // Por simplicidade, vamos simular um login anônimo ou com credenciais fixas
            signInAnonymously();
        });

        btnRanking.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RankingActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("MainActivity", "signInAnonymously:success");
                        currentUser = mAuth.getCurrentUser();
                        updateUI(currentUser);
                        Toast.makeText(MainActivity.this, "Login anônimo realizado.", Toast.LENGTH_SHORT).show();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("MainActivity", "signInAnonymously:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Falha no login anônimo.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        currentUser = user;
        if (gameView != null && gameView.getJogo() != null) {
            gameView.getJogo().setCurrentUser(currentUser);
        }
        // Atualizar visibilidade de botões ou textos conforme o estado de login
        // Ex: btnLogin.setText(currentUser != null ? "Logout" : "Login");
    }
}

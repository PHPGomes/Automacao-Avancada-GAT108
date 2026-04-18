package com.example.autotarget;

public class AlvoRapido extends Alvo{
    private int vel;

    public AlvoRapido(int x, int y, int tamX, int tamY, GameView gameView){
        super(x, y, tamX, tamY, gameView);
        vel = 12;
    }
}

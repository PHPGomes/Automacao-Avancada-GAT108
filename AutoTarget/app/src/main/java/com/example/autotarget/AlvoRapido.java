package com.example.autotarget;

public class AlvoRapido extends Alvo{
    private int vel;

    public AlvoRapido(int x, int y, int tamX, int tamY,int tipo, GameView gameView){
        super(x, y, tamX, tamY, tipo, gameView);
        vel = 12;
    }
}

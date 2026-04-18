package com.example.autotarget;

public class AlvoComum extends Alvo{
    private int vel;

    public AlvoComum(int x, int y, int tamX, int tamY, GameView gameView){
        super(x, y, tamX, tamY, gameView);
        vel = 6;
    }
}

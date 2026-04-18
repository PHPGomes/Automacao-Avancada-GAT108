package com.example.autotarget;

public class AlvoComum extends Alvo{
    private int vel;

    public AlvoComum(int x, int y, int tamX, int tamY,int tipo, GameView gameView){
        super(x, y, tamX, tamY, tipo, gameView);
        vel = 6;
    }
}

package com.example.autotarget;

public class MedidaRecon {

    private Alvo alvo;
    private Canhao canhao;

    private double distanciaMedia;
    private double variancia;

    private double y;

    public MedidaRecon(Alvo alvo,
                       Canhao canhao,
                       double distanciaMedia,
                       double variancia) {

        this.alvo = alvo;
        this.canhao = canhao;
        this.distanciaMedia = distanciaMedia;
        this.variancia = variancia;
    }



    public void calcularY() {

        double xc = canhao.getX();
        double yc = canhao.getY();

        y = distanciaMedia * distanciaMedia
                - (xc * xc + yc * yc);
    }

    public double getY() {
        return y;
    }

    public double getVariancia() {
        return variancia;
    }

    public double getDistanciaMedia() {
        return distanciaMedia;
    }

    public Canhao getCanhao() {
        return canhao;
    }

    public Alvo getAlvo() {
        return alvo;
    }
}
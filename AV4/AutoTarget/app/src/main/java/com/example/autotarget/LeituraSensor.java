package com.example.autotarget;

public class LeituraSensor {

    private double x;
    private double y;
    private double vx;
    private double vy;
    private long timestamp;

    public LeituraSensor(double x,double y,double vx,double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        timestamp = System.currentTimeMillis();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public long getTimestamp() { return timestamp; }
}
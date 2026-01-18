package org.example;

import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
public class Ball {
    private final double radius;
    private double x,y;
    private double vx, vy;
    private final Circle view;

    public Ball(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.view = new Circle(radius, Color.RED);
    }

    public void update(double dt, double gravity) {
        vy += gravity*dt;
        x += vx*dt;
        y += vy*dt;
        syncView();
    }

    private void syncView() {
        view.setCenterX(x);
        view.setCenterY(y);
    }

    public Circle getView() {
        return view;
    }

    public double getRadius() {
        return radius;
    }

    public void setX(double x) {
        this.x = x;
        syncView();
    }
    public void setY(double y) {
        this.y = y;
        syncView();
    }
    public void setVx(double vx) {
        this.vx = vx;
        syncView();
    }
    public void setVy(double vy) {
        this.vy = vy;
        syncView();
    }
    public double getY() {
        return y;
    }
    public double getX() {
        return x;
    }
    public double getVy() {
        return vy;
    }
    public double getVx() {
        return vx;
    }

}

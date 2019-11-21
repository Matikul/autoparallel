package nbody;

import java.io.Serializable;

public class Body implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final double G = 6.67e-11;

    public double x, y;
    public double vx, vy;
    public double ax, ay;
    public double mass;

    public Body(double x, double y, double mass) {
        this.x = x;
        this.y = y;
        vx = 0.0;
        vy = 0.0;
        ax = 0.0;
        ay = 0.0;
        this.mass = mass;
    }

    public Body(Body other) {
        this.x = other.x;
        this.y = other.y;
        this.vx = other.vx;
        this.vy = other.vy;
        this.ax = other.ax;
        this.ay = other.ay;
        this.mass = other.mass;
    }

    public double pairForceX(Body otherBody) {
        double dx = otherBody.x - this.x;
        double distance = this.distance(otherBody);
        if (distance == 0.0) {
            return 0.0;
        }
        return this.pairForce(otherBody) * dx / distance;
    }

    public double pairForceY(Body otherBody) {
        double dy = otherBody.y - this.y;
        double distance = this.distance(otherBody);
        if (distance == 0.0) {
            return 0.0;
        }
        return this.pairForce(otherBody) * dy / distance;
    }

    public double pairForce(Body otherBody) {
        double distance = this.distance(otherBody);
        if (distance == 0.0) {
            return 0.0;
        }
        return (G * this.mass * otherBody.mass) / (distance * distance);
    }

    public double distance(Body otherBody) {
        double dx = this.x - otherBody.x;
        double dy = this.y - otherBody.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

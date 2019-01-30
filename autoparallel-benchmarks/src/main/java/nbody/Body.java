package nbody;

class Body {
    private static final double G = 6.67e-11;

    double x, y;
    double vx, vy;
    double ax, ay;
    double mass;

    Body(double x, double y, double mass) {
        this.x = x;
        this.y = y;
        vx = 0.0;
        vy = 0.0;
        ax = 0.0;
        ay = 0.0;
        this.mass = mass;
    }

    Body(Body other) {
        this.x = other.x;
        this.y = other.y;
        this.vx = other.vx;
        this.vy = other.vy;
        this.ax = other.ax;
        this.ay = other.ay;
        this.mass = other.mass;
    }

    double pairForceX(Body otherBody) {
        double dx = otherBody.x - this.x;
        double distance = this.distance(otherBody);
        if (distance == 0.0) {
            return 0.0;
        }
        return this.pairForce(otherBody) * dx / distance;
    }

    double pairForceY(Body otherBody) {
        double dy = otherBody.y - this.y;
        double distance = this.distance(otherBody);
        if (distance == 0.0) {
            return 0.0;
        }
        return this.pairForce(otherBody) * dy / distance;
    }

    double pairForce(Body otherBody) {
        double distance = this.distance(otherBody);
        if (distance == 0.0) {
            return 0.0;
        }
        return (G * this.mass * otherBody.mass) / (distance * distance);
    }

    double distance(Body otherBody) {
        double dx = this.x - otherBody.x;
        double dy = this.y - otherBody.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

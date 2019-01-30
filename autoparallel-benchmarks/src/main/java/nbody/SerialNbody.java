package nbody;

public class SerialNbody {

    public final Body[] bodies;

    private static final double dt = 0.001;

    SerialNbody(Body[] bodies) {
        this.bodies = bodies;
    }

    void simulate(int steps) {
        for (int i = 0; i < steps; i++) {
            moveBodies();
        }
    }

    private void moveBodies() {
        int dataSize = bodies.length;
        Body[] beginningState = new Body[dataSize];//Arrays.copyOf(bodies, bodies.length);
        for (int i = 0; i < dataSize; i++) {
            beginningState[i] = new Body(bodies[i]);
        }
        for (int i = 0; i < dataSize; i++) {
            Body body = bodies[i];
            updateState(body, beginningState);
        }
    }

    private void updateState(Body body, Body[] beginningState) {
        double netForceX = 0.0;
        double netForceY = 0.0;
        for (Body otherBody : beginningState) {
            if (otherBody == body) {
                continue;
            } else {
                netForceX += body.pairForceX(otherBody);
                netForceY += body.pairForceY(otherBody);
            }
        }
        body.ax = netForceX / body.mass;
        body.ay = netForceY / body.mass;
        body.vx += body.ax * dt;
        body.vy += body.ay * dt;
        body.x += body.vx * dt;
        body.y += body.vy * dt;
    }

}

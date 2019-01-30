package nbody;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelNbody {

    public final Body[] bodies;

    private static final double dt = 0.001;
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    public ParallelNbody(Body[] bodies) {
        this.bodies = bodies;
    }

    public void simulate(int steps) {
        for (int i = 0; i < steps; i++) {
            moveBodies();
        }
    }

    public Body[] getBodies() {
        return bodies;
    }

    private void moveBodies() {
        ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);
        List<Callable<Integer>> tasks = new LinkedList<>();
        int dataSize = bodies.length;
        Body[] beginningState = new Body[dataSize];//we need deep copy
        for (int i = 0; i < dataSize; i++) {
            beginningState[i] = new Body(bodies[i]);
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * (dataSize / NUM_THREADS);
            int stop = (i + 1) * (dataSize / NUM_THREADS) - 1;
            if (stop > dataSize - 1) {
                stop = dataSize - 1;
            }
            int finalStop = stop;
            tasks.add(() -> partialUpdate(start, finalStop, beginningState));
        }
        try {
            service.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();
    }

    private int partialUpdate(int start, int stop, Body[] beginningState) {
        for (int i = start; i <= stop; i++) {
            Body body = bodies[i];
            updateState(body, beginningState);
        }
        return 0;
    }

    private void updateState(Body body, Body[] beginningState) {
        double netForceX = 0.0;
        double netForceY = 0.0;
        for (Body otherBody : beginningState) {
            if (otherBody != body) {
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

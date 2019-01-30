package nbody;

import java.util.Random;

public class NbodyTest {

    private static final int steps = 10000;
    private static final int numBodies = 1000;

    public static void main(String[] args) {

        Body[] bodies = new Body[numBodies];
        Body[] bodies2 = new Body[numBodies];
        Random random = new Random();
        for (int i = 0; i < numBodies; i++) {
            bodies[i] = new Body(random.nextDouble() / 10,
                    random.nextDouble() / 10,
                    (random.nextDouble() * 100 + 200));
            bodies2[i] = new Body(bodies[i]);
        }

        System.out.println(bodies[0].x + ", " + bodies[0].y);
        System.out.println(bodies2[0].x + ", " + bodies2[0].y);

        SerialNbody serialNbody = new SerialNbody(bodies);
        Long start = System.currentTimeMillis();
        serialNbody.simulate(steps);
        System.out.println(System.currentTimeMillis() - start);

        ParallelNbody parallelNbody = new ParallelNbody(bodies2);
        start = System.currentTimeMillis();
        parallelNbody.simulate(steps);
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(bodies[0].x + ", " + bodies[0].y);
        System.out.println(bodies2[0].x + ", " + bodies2[0].y);
    }
}

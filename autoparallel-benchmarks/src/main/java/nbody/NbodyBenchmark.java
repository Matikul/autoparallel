package nbody;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;

public class NbodyBenchmark {

    private static final int steps = 10000;

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(NbodyBenchmark.class.getSimpleName())
                .threads(1)
                .forks(5)
                .warmupIterations(3)
                .mode(Mode.AverageTime)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public void serialNbodyBenchmark(TestState state) {
        state.serialNbody.simulate(steps);
    }

    @Benchmark
    public void parallelNbodyBenchmark(TestState state) {
        state.parallelNbody.simulate(steps);
    }

    @State(value = Scope.Benchmark)
    public static class TestState {
        int numBodies = 1000;
        Body[] bodies = generateBodies(numBodies);
        SerialNbody serialNbody = new SerialNbody(bodies);
        ParallelNbody parallelNbody = new ParallelNbody(bodies);

        Body[] generateBodies(int numBodies) {
            Body[] bodies = new Body[numBodies];
            Random random = new Random();
            for (int i = 0; i < numBodies; i++) {
                bodies[i] = new Body(random.nextDouble() / 10,
                        random.nextDouble() / 10,
                        (random.nextDouble() * 100 + 200));
            }
            return bodies;
        }
    }
}

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
    public void serialNbodyBenchmark() {
        SerialNbody.simulate(steps);
    }

    @Benchmark
    public void parallelNbodyBenchmark() {
        ParallelNbody.simulate(steps);
    }
}

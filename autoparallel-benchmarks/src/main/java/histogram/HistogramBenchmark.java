package histogram;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;

public class HistogramBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(HistogramBenchmark.class.getSimpleName())
                .threads(1)
                .forks(10)
                .warmupIterations(10)
                .mode(Mode.AverageTime)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public void SerialHistogramBenchmark(TestState state) {
        state.serialHistogram.calculate();
    }

    @Benchmark
    public void ParallelHistogramBenchmark(TestState state) {
        state.parallelHistogram.calculate();
    }

    @State(value = Scope.Benchmark)
    public static class TestState {
        int size = 2000000000;
        int dataBound = 99;
        int data[] = initArray();
        SerialHistogram serialHistogram = new SerialHistogram(data, dataBound);
        ParallelHistogram parallelHistogram = new ParallelHistogram(data, dataBound);

        private int[] initArray() {
            int arr[] = new int[size];
            Random random = new Random();
            for (int i = 0; i < size; i++) {
                arr[i] = random.nextInt(dataBound + 1);
            }
            return arr;
        }
    }
}

package matrix;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import util.MatrixUtils;

public class MatrixBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(MatrixBenchmark.class.getSimpleName())
                .threads(1)
                .forks(5)
                .warmupIterations(3)
                .mode(Mode.AverageTime)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public void MultiplySquareMatricesSerial(TestState state) {
        state.serialMultiplier.multiply();
    }

    @Benchmark
    public void MultiplySquareMatricesParallel(TestState state) {
        state.parallelMultiplier.multiply();
    }

    @State(value = Scope.Benchmark)
    public static class TestState {
        int size = 2048;
        int bound = 20;
        int[][] A = MatrixUtils.randomIntArrayMatrix(size, size, bound);
        int[][] B = MatrixUtils.randomIntArrayMatrix(size, size, bound);
        SerialMultiplier serialMultiplier = new SerialMultiplier(A, B);
        ParallelMultiplier parallelMultiplier = new ParallelMultiplier(A, B);
    }
}

package histogram;

import java.util.Arrays;
import java.util.Random;

public class HistogramTest {
    private static final int BOUND_1 = 100000000;
    private static final int DATA_LIMIT_1 = 10;

    public static void main(String[] args) {

        int[] data = {0, 1, 1, 2, 3, 1, 2, 0};
        int[] data2 = new int[BOUND_1];
        Random random = new Random();
        for (int i = 0; i < BOUND_1; i++) {
            data2[i] = random.nextInt(DATA_LIMIT_1 + 1);
        }

        SerialHistogram histogram = new SerialHistogram(data2, DATA_LIMIT_1);
        System.out.println("Starting...");
        Long now = System.currentTimeMillis();
        histogram.calculate();
        System.out.println("Took " + (double) (System.currentTimeMillis() - now) / 1e3);
        System.out.println(Arrays.toString(histogram.getResult()));
        System.out.println(Arrays.stream(histogram.getResult()).sum());

        ParallelHistogram histogram2 = new ParallelHistogram(data2, DATA_LIMIT_1);
        System.out.println("Starting...");
        Long now2 = System.currentTimeMillis();
        histogram2.calculate();
        System.out.println("Took " + (double) (System.currentTimeMillis() - now2) / 1e3);
        System.out.println(Arrays.toString(histogram2.getResult()));
        System.out.println(Arrays.stream(histogram2.getResult()).sum());
    }
}

package util;

import java.util.Random;

public class ArrayUtils {

    public static double[] randomIntArray(int size) {
        double[] arr = new double[size];
        Random random = new Random();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = random.nextDouble();
        }
        return arr;
    }

    public static double[] randomIntArrayWithBounds(int size, double bound) {
        double[] arr = new double[size];
        Random random = new Random();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = random.nextDouble() * bound;
        }
        return arr;
    }
}

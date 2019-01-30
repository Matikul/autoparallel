package fft;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelFFT {

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int THRESHOLD = 1024*32;

    private ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(NUM_THREADS);

    public void transform(double[] real, double[] imag) {
        int n = real.length;
        if (n == 0)
            return;
        if (n != imag.length)
            throw new IllegalArgumentException("Array lengths should be equal");
        else if ((n & (n - 1)) != 0)
            throw new IllegalArgumentException("Array length should be a power of 2");
        else
            fft(real, imag);
    }

    private void fft(double[] real, double[] imag) {
        int n = real.length;
        int levels = 31 - Integer.numberOfLeadingZeros(n);//log2(n)

        double[] cosTable = new double[n / 2];
        double[] sinTable = new double[n / 2];
        for (int i = 0; i < n / 2; i++) {
            cosTable[i] = Math.cos(2 * Math.PI * i / n);
            sinTable[i] = Math.sin(2 * Math.PI * i / n);
        }

        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - levels);
            if (j > i) {
                double temp = real[i];
                real[i] = real[j];
                real[j] = temp;
                temp = imag[i];
                imag[i] = imag[j];
                imag[j] = temp;
            }
        }

        for (int size = 2; size <= n; size *= 2) {
            int halfSize = size / 2;
            int tableStep = n / size;
            List<Callable<Integer>> tasks = new LinkedList<>();
            if (n / size < THRESHOLD) {
                for (int i = 0; i < n; i += size) {
                    for (int j = i, k = 0; j < i + halfSize; j++, k += tableStep) {
                        int l = j + halfSize;
                        double tpre = real[l] * cosTable[k] + imag[l] * sinTable[k];
                        double tpim = -real[l] * sinTable[k] + imag[l] * cosTable[k];
                        real[l] = real[j] - tpre;
                        imag[l] = imag[j] - tpim;
                        real[j] += tpre;
                        imag[j] += tpim;
                    }
                }
            } else {
                for (int i = 0; i < n; i += THRESHOLD) {
                    int startI = i;
                    int stopI = i + THRESHOLD;
                    int finalSize = size;
                    tasks.add(() -> subTask(real, imag, cosTable, sinTable, finalSize, halfSize, tableStep, startI, stopI));
                }
                try {
                    EXECUTOR_SERVICE.invokeAll(tasks);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (size == n)
                break;
        }
    }


    private int subTask(double[] real, double[] imag,
                               double[] cosTable, double[] sinTable,
                               int size, int halfSize, int tableStep,
                               int startI, int stopI) {
        for (int i = startI; i < stopI; i += size) {
            for (int j = i, k = 0; j < i + halfSize; j++, k += tableStep) {
                int l = j + halfSize;
                double tpre = real[l] * cosTable[k] + imag[l] * sinTable[k];
                double tpim = -real[l] * sinTable[k] + imag[l] * cosTable[k];
                real[l] = real[j] - tpre;
                imag[l] = imag[j] - tpim;
                real[j] += tpre;
                imag[j] += tpim;
            }
        }
        return 0;
    }
}

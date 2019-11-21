import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test4 {
    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    public static ExecutorService SERVICE = Executors.newFixedThreadPool(NUM_THREADS);
    private static int START_RANGE;
    private static int END_RANGE;

    public static void main(String[] args) {
        int trash = 20;
        for (int i = 0; i < NUM_THREADS; i++) {
            START_RANGE = i * (16 / NUM_THREADS);
            END_RANGE = (i + 1) * (16 / NUM_THREADS) - 1;
            subTask(START_RANGE, END_RANGE);
        }
        int trash2 = 600;
    }

    private void trashMethod(int x) {
        int a = x + 8;
    }

    private static void subTask(int start, int end) {
        for (int i = start; i < end; i++) {
            System.out.println(i);
        }
    }

    private static void subTask2(int start, int end) {
        for (int i = start; i < end; i++) {
        }
    }

    private static void yy() {
        for (int i = 0; i < NUM_THREADS; i++) {
            START_RANGE = i * (16 / NUM_THREADS);
            END_RANGE = (i + 1) * (16 / NUM_THREADS) - 1;
            subTask(START_RANGE, END_RANGE);
        }
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpectedClass {

    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    public static ExecutorService SERVICE;
    private static int START_RANGE;
    private static int END_RANGE;

    public ExpectedClass() {
    }

    static {
        SERVICE = Executors.newFixedThreadPool(NUM_THREADS);
    }

    public static void main(String[] args) throws Exception {
        new ArrayList();
        List tasks = new ArrayList();

        for(int i = 0; i < NUM_THREADS; ++i) {
            final int start = i * (16 / NUM_THREADS);
            final int end = (i + 1) * (16 / NUM_THREADS) - 1;
            tasks.add(new Callable<Integer>() {
                public Integer call() throws Exception {
                    return IntegrationTestClass_modified.subTask(start, end);
                }
            });
        }

        SERVICE.invokeAll(tasks);
        SERVICE.shutdown();
    }

    private static void subTask(int start, int end) {
        for(int i = start; i <= end; ++i) {
            System.out.println(i);
        }

    }
}

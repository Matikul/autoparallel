package histogram;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelHistogram {

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    private final int[] data;
    private final int[] results;

    public ParallelHistogram(int[] data, int limit) {
        this.data = data;
        results = new int[limit + 1];
    }

    public void calculate() {
        ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);
        List<Callable<int[]>> tasks = new LinkedList<>();
        List<Future<int[]>> partialResults = new LinkedList<>();
        int dataSize = data.length;
        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * (dataSize / NUM_THREADS);
            int stop = (i + 1) * (dataSize / NUM_THREADS) - 1;
            if (stop > dataSize - 1) {
                stop = dataSize - 1;
            }
            int finalStop = stop;
            tasks.add(() -> processChunk(start, finalStop));
        }
        try {
            partialResults = service.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();
        for (Future<int[]> partialResult : partialResults) {
            try {
                int[] part = partialResult.get();
                for (int i = 0; i < results.length; i++) {
                    results[i] += part[i];
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public int[] getResult() {
        return results;
    }

    private int[] processChunk(int start, int stop) {
        int[] partialResult = new int[results.length];
        for (int i = start; i <= stop; i++) {
            partialResult[data[i]]++;
        }
        return partialResult;
    }
}

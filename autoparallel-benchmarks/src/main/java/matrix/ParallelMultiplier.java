package matrix;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelMultiplier {

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    private int[][] A, B, C;
    private int resultRows, resultColumns;

    public ParallelMultiplier(int[][] pA, int[][] pB) {
        this.A = pA;
        this.B = pB;
        if (A[0].length != B.length) {
            throw new RuntimeException("Cannot perform multiplication because dimensions are not equal.");
        }
        resultRows = A.length;
        resultColumns = B[0].length;
        C = new int[resultRows][resultColumns];
    }

    public int[][] multiply() {
        ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);
        List<Callable<Integer>> tasks = new LinkedList<>();
        int step = A.length / NUM_THREADS;
        for (int rowNum = 0; rowNum < resultRows; rowNum += step) {
            for (int colNum = 0; colNum < resultColumns; colNum++) {
                int finalRowNum = rowNum;
                int finalColNum = colNum;
                tasks.add(() -> singleCalculation(finalRowNum, finalColNum, step));
            }
        }
        try {
            service.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();
        return C;
    }

    private int singleCalculation(int rowNum, int colNum, int step) {
        for (int currRow = rowNum; currRow < rowNum + step && currRow < resultRows; currRow++) {
            for (int r = 0; r < B.length; r++) {
                C[currRow][colNum] += A[currRow][r] * B[r][colNum];
            }
        }
        return 0;
    }
}

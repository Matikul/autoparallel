package util;

import java.util.Random;

public class MatrixUtils {

    public static void printMatrix(int[][] A) {
        int rows = A.length;
        int columns = A[0].length;
        for (int rowNum = 0; rowNum < rows; rowNum++) {
            for (int colNum = 0; colNum < columns; colNum++) {
                System.out.printf("%6d", A[rowNum][colNum]);
            }
            System.out.print("\n");
        }
    }

    public static int[][] randomIntArrayMatrix(int rows, int columns, int bound) {
        int[][] A = new int[rows][columns];
        Random gen = new Random();
        for (int rowNum = 0; rowNum < rows; rowNum++) {
            for (int colNum = 0; colNum < columns; colNum++) {
                A[rowNum][colNum] = gen.nextInt(bound);
            }
        }
        return A;
    }
}
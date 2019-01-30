package matrix;

import util.MatrixUtils;

public class MatrixTest {

    public static void main(String[] args) {
//        ArrayList<ArrayList<Integer>> A = new ArrayList<>();
//        ArrayList<ArrayList<Integer>> B = new ArrayList<>();
//        A.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));
//        A.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));
//        A.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));
//        A.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));
//        A.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));
//        B.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)));
//        B.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)));
//        B.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)));
//        B.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)));
//
//        int[][] X = {{1, 2, 3}, {1, 2, 3}, {1, 2, 3}};
//
//        MatrixUtils.printMatrix(A);
//        System.out.println();
//        MatrixUtils.printMatrix(B);
//        System.out.println();
//        MatrixUtils.printMatrix(MatrixUtils.multiply(A, B));
//        System.out.println();
//        MatrixUtils.printMatrix(MatrixUtils.multiply(X, X));
        //------------------------------------------------------------------
        int SIZE = 2000;
        int[][] AA = MatrixUtils.randomIntArrayMatrix(SIZE, SIZE, 20);
        int[][] BB = MatrixUtils.randomIntArrayMatrix(SIZE, SIZE, 20);
        SerialMultiplier multiplier = new SerialMultiplier(AA, BB);
        ParallelMultiplier multiplier2 = new ParallelMultiplier(AA, BB);
        Long now = System.currentTimeMillis();
//        multiplier.multiply();
//        System.out.println((double)(System.currentTimeMillis() - now) / 1000);
        now = System.currentTimeMillis();
        multiplier2.multiply();
        System.out.println((double)(System.currentTimeMillis() - now) / 1000);

//        MatrixUtils.printMatrix(multiplier.multiply());
//        System.out.println();
//        MatrixUtils.printMatrix(multiplier2.multiply());
        //------------------------------------------------------------------


//        long start = System.nanoTime();
//        par.multiply();
//        System.out.println((double)(System.nanoTime() - start) / 1e9);
//        start = System.nanoTime();
//        MatrixUtils.multiply(AA, BB);
//        System.out.println((double)(System.nanoTime() - start) / 1e9);

//        MatrixUtils.printMatrix(AA);
//        System.out.println();
//        MatrixUtils.printMatrix(BB);
//        System.out.println();
//        MatrixUtils.printMatrix(MatrixUtils.multiply(AA, BB));
    }
}

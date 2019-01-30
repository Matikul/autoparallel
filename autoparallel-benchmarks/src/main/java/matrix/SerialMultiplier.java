package matrix;

public class SerialMultiplier {

    private int[][] A, B, C;
    private int resultRows, resultColumns;

    public SerialMultiplier(int[][] pA, int[][] pB) {
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
        for (int rowNum = 0; rowNum < resultRows; rowNum++) {
            for (int colNum = 0; colNum < resultColumns; colNum++) {
                for (int r = 0; r < B.length; r++) {
                    C[rowNum][colNum] += A[rowNum][r] * B[r][colNum];
                }
            }
        }
        return C;
    }
}

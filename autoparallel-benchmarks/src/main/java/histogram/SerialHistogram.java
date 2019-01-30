package histogram;

public class SerialHistogram {

    private final int[] data;
    private final int[] results;

    public SerialHistogram(int[] data, int limit) {
        this.data = data;
        results = new int[limit + 1];
    }

    public void calculate() {
        for (int i = 0; i < data.length; i++) {
            results[data[i]]++;
        }
    }

    public int[] getResult() {
        return results;
    }
}

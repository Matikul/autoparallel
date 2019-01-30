package fft;

public class SerialFFT {

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
            for (int i = 0; i < n; i += size) {
                for (int j = i, k = 0; j < i + halfSize; j++, k += tableStep) {
                    int l = j + halfSize;
                    double tpre =  real[l] * cosTable[k] + imag[l] * sinTable[k];
                    double tpim = -real[l] * sinTable[k] + imag[l] * cosTable[k];
                    real[l] = real[j] - tpre;
                    imag[l] = imag[j] - tpim;
                    real[j] += tpre;
                    imag[j] += tpim;
                }
            }
            if (size == n)
                break;
        }
    }
}

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class LagCorrelationCalculator {
    public static void main(String[] args) throws Exception {
        String csvFile = "SampleData.csv";
        List<double[]> signals = new ArrayList<>();

        // CSV 읽기
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                double[] row = new double[8];
                for (int i = 0; i < 8; i++) {
                    row[i] = Double.parseDouble(tokens[i + 1]); // Skip Time_Point
                }
                signals.add(row);
            }
        }

        int numRows = signals.size();
        double[] signal1 = new double[numRows];
        double[][] otherSignals = new double[7][numRows];

        

        for (int i = 0; i < numRows; i++) {
            signal1[i] = signals.get(i)[0];
            for (int j = 0; j < 7; j++) {
                otherSignals[j][i] = signals.get(i)[j + 1];
            }
        }

        signal1 = normalize(signal1);
        for (int j = 0; j < 7; j++) {
            otherSignals[j] = normalize(otherSignals[j]);
        }


        // 시차 범위 설정
        int[] lags = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};

        for (int sigIndex = 0; sigIndex < 7; sigIndex++) {
            System.out.println("=== Signal 1 vs Signal " + (sigIndex + 2) + " ===");
            for (int lag : lags) {
                double corr = laggedCorrelation(signal1, otherSignals[sigIndex], lag);
                System.out.printf("Lag %2d: %.4f\n", lag, corr);
            }
            System.out.println();
        }

        // 최대 상관계수 찾기
        MaxCorrelationResult maxResult = findMaxCorrelation(signal1, otherSignals, lags);
        System.out.printf("Max Correlation: %.4f at Lag %d between Signal %d and Signal %d\n",
                maxResult.maxCorrelation, maxResult.lag, maxResult.signal1, maxResult.signal2); 
    }

    public static double[] normalize(double[] input) {
        double mean = Arrays.stream(input).average().orElse(0);
        double std = Math.sqrt(Arrays.stream(input).map(x -> Math.pow(x - mean, 2)).sum() / input.length);
        double[] normalized = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            normalized[i] = (input[i] - mean) / (std + 1e-10);
        }
        return normalized;
    }

    // 정규화 상관계수 계산
    public static double laggedCorrelation(double[] x, double[] y, int lag) {
        int n = x.length;
        int startX = Math.max(0, -lag);
        int startY = Math.max(0, lag);
        int length = n - Math.abs(lag);

        double[] subX = Arrays.copyOfRange(x, startX, startX + length);
        double[] subY = Arrays.copyOfRange(y, startY, startY + length);

        return pearsonCorrelation(subX, subY);
    }

    // 피어슨 상관계수
    public static double pearsonCorrelation(double[] x, double[] y) {
        int n = x.length;
        double meanX = Arrays.stream(x).average().orElse(0);
        double meanY = Arrays.stream(y).average().orElse(0);

        double num = 0, denomX = 0, denomY = 0;
        for (int i = 0; i < n; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            num += dx * dy;
            denomX += dx * dx;
            denomY += dy * dy;
        }

        return num / Math.sqrt(denomX * denomY + 1e-10); // add small epsilon to avoid divide-by-zero
    }
    // MaxCorrelationResult class moved inside LagCorrelationCalculator
    public static class MaxCorrelationResult {
        public double maxCorrelation;
        public int lag;
        public int signal1;
        public int signal2;

        public MaxCorrelationResult(double maxCorrelation, int lag, int signal1, int signal2) {
            this.maxCorrelation = maxCorrelation;
            this.lag = lag;
            this.signal1 = signal1;
            this.signal2 = signal2;
        }
    }

    // findMaxCorrelation method moved inside LagCorrelationCalculator
    public static MaxCorrelationResult findMaxCorrelation(double[] signal1, double[][] otherSignals, int[] lags) {
        double maxCorr = laggedCorrelation(signal1, otherSignals[0], lags[0]);
        int maxLag = 0;
        int maxSigIndex = 0;

        for (int sigIndex = 0; sigIndex < otherSignals.length; sigIndex++) {
            for (int lag : lags) {
                double corr = laggedCorrelation(signal1, otherSignals[sigIndex], lag);
                if (Math.abs(corr) > Math.abs(maxCorr)) {
                    maxCorr = corr;
                    maxLag = lag;
                    maxSigIndex = sigIndex;
                }
            }
        }
        // signal1 is always index 0, otherSignals are 1-based (signal2, signal3, ...)
        return new MaxCorrelationResult(maxCorr, maxLag, 1, maxSigIndex + 2);
    }
}
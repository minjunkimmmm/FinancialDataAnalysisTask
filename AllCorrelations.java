import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AllCorrelations {
    public static void main(String[] args) {
        String csvFile = "SampleData.csv"; // Use absolute path if needed

        ArrayList<Double> signal1 = new ArrayList<>();
        ArrayList<ArrayList<Double>> signals = new ArrayList<>();

        // Prepare 7 empty lists for signals 2 to 8
        for (int i = 0; i < 7; i++) {
            signals.add(new ArrayList<>());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Skip header
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                signal1.add(Double.parseDouble(values[1])); // Signal 1
                for (int i = 0; i < 7; i++) {
                    signals.get(i).add(Double.parseDouble(values[i + 2])); // Signals 2–8
                }
            }

            double[] x = signal1.stream().mapToDouble(Double::doubleValue).toArray();

            for (int i = 0; i < 7; i++) {
                double[] y = signals.get(i).stream().mapToDouble(Double::doubleValue).toArray();
                double corr = pearsonCorrelation(x, y);
                System.out.printf("Correlation between Signal 1 and Signal %d: %.4f\n", i + 2, corr);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Pearson correlation coefficient
    public static double pearsonCorrelation(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0;
        double sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        return denominator == 0 ? 0 : numerator / denominator;
    }
}

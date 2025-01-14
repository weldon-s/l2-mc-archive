import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public final class ListAnalyzer {
    private final String source;
    private final List<Double> values;

    private final double mean;
    private final double standardDeviation;
    private final double confidenceIntervalDifference;
    private final double median;
    private final double minimum;
    private final double maximum;

    public ListAnalyzer(String s) {
        source = s;
        values = new ArrayList<Double>();

        load();
        mean = loadMean();
        standardDeviation = loadStandardDeviation();
        confidenceIntervalDifference = loadConfidenceIntervalDifference();
        median = loadMedian();
        minimum = loadMinimum();
        maximum = loadMaximum();
    }

    private void load() {
        Scanner scan = new Scanner(source);

        while (scan.hasNextDouble()) {
            values.add(scan.nextDouble());
        }

        scan.close();

        Collections.sort(values);
    }

    private double loadMean() {
        double total = 0;

        for (int i = 0; i < values.size(); i++) {
            total += values.get(i);
        }

        return total / values.size();
    }

    private double loadStandardDeviation() {
        if (values.size() == 1) {
            return 0;
        }

        double total = 0;

        for (int i = 0; i < values.size(); i++) {
            total += (values.get(i) - mean) * (values.get(i) - mean);
        }

        return Math.sqrt(total / (values.size() - 1));
    }

    private double loadMedian() {
        if (values.size() % 2 == 0) {
            double d1 = values.get(values.size() / 2 - 1);
            double d2 = values.get(values.size() / 2);

            return (d1 + d2) / 2;
        }

        return values.get(values.size() / 2);
    }

    private double loadConfidenceIntervalDifference() {
        return 1.96 * standardDeviation / Math.sqrt(values.size());
    }

    private double loadMinimum() {
        double ret = values.get(0);

        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) < ret) {
                ret = values.get(i);
            }
        }

        return ret;
    }

    private double loadMaximum() {
        double ret = values.get(0);

        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) > ret) {
                ret = values.get(i);
            }
        }

        return ret;
    }

    public double getMean() {
        return mean;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getConfidenceIntervalDifference() {
        return confidenceIntervalDifference;
    }

    public double getMedian() {
        return median;
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public String getAnalysisString() {
        String ret = "Mean: " + mean + " ± " + confidenceIntervalDifference + "\n";
        ret += "Standard Deviation: " + standardDeviation + "\n";
        ret += "Median: " + median + "\n";
        ret += "Minimum: " + minimum + "\n";
        ret += "Maximum: " + maximum + "\n";

        return ret;
    }
}
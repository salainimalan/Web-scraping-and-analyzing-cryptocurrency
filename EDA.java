import com.opencsv.CSVReader;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.io.FileReader;
import java.util.*;

public class EDA {

    // ---------- Parse numeric safely ----------
    static double parse(String v) {
        if (v == null) return Double.NaN;
        v = v.replace("$", "")
             .replace(",", "")
             .replace("%", "")
             .trim();
        if (v.isEmpty() || v.equals("-")) return Double.NaN;
        return Double.parseDouble(v);
    }

    static double[] toArray(List<Double> list) {
        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }

    static void univariate(String name, double[] arr) {
        System.out.println("\nUNIVARIATE ANALYSIS : " + name);
        System.out.println("Mean : " + StatUtils.mean(arr));
        System.out.println("Min  : " + StatUtils.min(arr));
        System.out.println("Max  : " + StatUtils.max(arr));
        System.out.println("Std  : " + Math.sqrt(StatUtils.variance(arr)));
    }

    static void missingTreatment(String name, double[] arr) {
        double mean = StatUtils.mean(arr);
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (Double.isNaN(arr[i])) {
                arr[i] = mean;
                count++;
            }
        }
        System.out.println(name + " → Missing values replaced: " + count);
    }

    static void outlierDetection(String name, double[] arr) {
        double mean = StatUtils.mean(arr);
        double std = Math.sqrt(StatUtils.variance(arr));
        int shown = 0;

        for (double v : arr) {
            double z = (v - mean) / std;
            if (Math.abs(z) > 3 && shown < 3) {
                System.out.println(name + " Outlier: " + v);
                shown++;
            }
        }
    }

    static void logTransform(String name, double[] arr) {
        System.out.println("\nLOG TRANSFORMATION : " + name);
        int shown = 0;
        for (double v : arr) {
            if (v > 0 && shown < 3) {
                System.out.println(v + " -> " + Math.log(v));
                shown++;
            }
        }
    }

    public static void main(String[] args) throws Exception {

        CSVReader reader = new CSVReader(new FileReader("crypto_data.csv"));
        String[] header = reader.readNext();

        // ---------- Data map ----------
        Map<String, List<Double>> data = new LinkedHashMap<>();

        data.put(header[4], new ArrayList<>());   // Price
        data.put(header[5], new ArrayList<>());   // 1h
        data.put(header[6], new ArrayList<>());   // 24h
        data.put(header[7], new ArrayList<>());   // 7d
        data.put(header[8], new ArrayList<>());   // 30d
        data.put(header[9], new ArrayList<>());   // Volume
        data.put(header[10], new ArrayList<>());  // Market Cap
        data.put(header[11], new ArrayList<>());  // FDV
        data.put(header[12], new ArrayList<>());  // MC/FDV

        String[] row;
        while ((row = reader.readNext()) != null) {
            int i = 4;
            for (String key : data.keySet()) {
                data.get(key).add(parse(row[i++]));
            }
        }

        // =================================================
        System.out.println("VARIABLE IDENTIFICATION");
        System.out.println("Coin -> Categorical");
        for (String key : data.keySet()) {
            System.out.println(key + " -> Numerical");
        }

        // =================================================
        for (String key : data.keySet()) {
            univariate(key, toArray(data.get(key)));
        }

        // =================================================
        System.out.println("\nBI-VARIATE ANALYSIS");
        PearsonsCorrelation pc = new PearsonsCorrelation();

        double[] price = toArray(data.get("Price"));
        double[] mc = toArray(data.get("Market Cap"));
        double[] vol = toArray(data.get("24h Volume"));

        System.out.println("Price vs Market Cap : " + pc.correlation(price, mc));
        System.out.println("Price vs Volume     : " + pc.correlation(price, vol));
        System.out.println("Market Cap vs Volume: " + pc.correlation(mc, vol));

        // =================================================
        System.out.println("\nMISSING VALUE TREATMENT");
        for (String key : data.keySet()) {
            missingTreatment(key, toArray(data.get(key)));
        }

        // =================================================
        System.out.println("\nOUTLIER DETECTION (Z-SCORE)");
        for (String key : data.keySet()) {
            outlierDetection(key, toArray(data.get(key)));
        }

        // =================================================
        for (String key : data.keySet()) {
            logTransform(key, toArray(data.get(key)));
        }

        // =================================================
        System.out.println("\nVARIABLE CREATION (Price / Market Cap)");
        System.out.println("Sample values:");
        for (int i = 0; i < 3; i++) {
            System.out.println(price[i] / mc[i]);
        }

        System.out.println("\nEDA COMPLETED SUCCESSFULLY");
    }
}



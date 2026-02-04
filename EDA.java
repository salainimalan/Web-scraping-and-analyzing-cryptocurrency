import com.opencsv.CSVReader;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class EDA {


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

    public static void main(String[] args) throws Exception {

        CSVReader reader = new CSVReader(new FileReader("crypto_data.csv"));
        reader.readNext(); // skip header

        // ---------------- DATA CONTAINERS ----------------
        List<Double> price = new ArrayList<>();
        List<Double> ch1h = new ArrayList<>();
        List<Double> ch24h = new ArrayList<>();
        List<Double> ch7d = new ArrayList<>();
        List<Double> ch30d = new ArrayList<>();
        List<Double> volume = new ArrayList<>();
        List<Double> marketCap = new ArrayList<>();
        List<Double> fdv = new ArrayList<>();
        List<Double> mcFdv = new ArrayList<>();

        String[] row;
        while ((row = reader.readNext()) != null) {
            price.add(parse(row[4]));
            ch1h.add(parse(row[5]));
            ch24h.add(parse(row[6]));
            ch7d.add(parse(row[7]));
            ch30d.add(parse(row[8]));
            volume.add(parse(row[9]));
            marketCap.add(parse(row[10]));
            fdv.add(parse(row[11]));
            mcFdv.add(parse(row[12]));
        }

        // Convert to arrays
        double[] priceArr = toArray(price);
        double[] ch1hArr = toArray(ch1h);
        double[] ch24hArr = toArray(ch24h);
        double[] ch7dArr = toArray(ch7d);
        double[] ch30dArr = toArray(ch30d);
        double[] volArr = toArray(volume);
        double[] mcArr = toArray(marketCap);
        double[] fdvArr = toArray(fdv);
        double[] mcFdvArr = toArray(mcFdv);

        // =================================================
        System.out.println("VARIABLE IDENTIFICATION");
        System.out.println("Coin              -> Categorical");
        System.out.println("Price             -> Numerical");
        System.out.println("1h, 24h, 7d, 30d  -> Numerical");
        System.out.println("Volume            -> Numerical");
        System.out.println("Market Cap        -> Numerical");
        System.out.println("FDV               -> Numerical");
        System.out.println("Market Cap / FDV  -> Numerical");

        // =================================================
        univariate("Price", priceArr);
        univariate("1h Change", ch1hArr);
        univariate("24h Change", ch24hArr);
        univariate("7d Change", ch7dArr);
        univariate("30d Change", ch30dArr);
        univariate("24h Volume", volArr);
        univariate("Market Cap", mcArr);
        univariate("FDV", fdvArr);
        univariate("Market Cap / FDV", mcFdvArr);

        System.out.println("\nBI-VARIATE ANALYSIS");
        PearsonsCorrelation pc = new PearsonsCorrelation();

        System.out.println("Price vs Market Cap : " +
                pc.correlation(priceArr, mcArr));
        System.out.println("Price vs Volume     : " +
                pc.correlation(priceArr, volArr));
        System.out.println("Market Cap vs Volume: " +
                pc.correlation(mcArr, volArr));
        System.out.println("Price vs 24h Change : " +
                pc.correlation(priceArr, ch24hArr));

        // =================================================
        System.out.println("\nMISSING VALUE TREATMENT");
        double meanPrice = StatUtils.mean(priceArr);
        int miss = 0;
        for (int i = 0; i < priceArr.length; i++) {
            if (Double.isNaN(priceArr[i])) {
                priceArr[i] = meanPrice;
                miss++;
            }
        }
        System.out.println("Missing values replaced (Price): " + miss);

        // =================================================
        System.out.println("\nOUTLIER DETECTION (Z-SCORE, PRICE)");
        double std = Math.sqrt(StatUtils.variance(priceArr));
        for (double v : priceArr) {
            double z = (v - meanPrice) / std;
            if (Math.abs(z) > 3)
                System.out.println("Outlier : " + v);
        }

        // =================================================
        System.out.println("\nVARIABLE TRANSFORMATION (LOG PRICE)");
        System.out.println("Before -> After (sample values)");

        int shown = 0;
        for (double v : priceArr) {
            if (v > 0 && shown < 5) {
                System.out.println(v + " -> " + Math.log(v));
                shown++;
            }
        }

        // =================================================
        System.out.println("\nVARIABLE CREATION (Price / Market Cap)");
        System.out.println("Sample values:");
        shown = 0;
        for (int i = 0; i < priceArr.length && shown < 5; i++) {
            if (mcArr[i] != 0 && !Double.isNaN(mcArr[i])) {
                System.out.println(priceArr[i] / mcArr[i]);
                shown++;
            }
        }

        System.out.println("\nEDA COMPLETED SUCCESSFULLY");
    }
}


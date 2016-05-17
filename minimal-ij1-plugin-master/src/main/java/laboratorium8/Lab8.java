package laboratorium8;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.Optional;


public class Lab8 implements PlugInFilter {

    private final int L = 256;
    private final int T = 255;
    private final int COLOR_WHITE = 255;

    ImagePlus imp;
    ImageProcessor obraz;

    double liczbaPikseli;

    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL;
    }

    public void run(ImageProcessor ip) {
        prepareImage(ip);
        filter();
    }

    private void prepareImage(ImageProcessor ip) {
        doDialog();
    }

    private void filter() {
//        int prog = wyznaczProg();
        int prog = wyznaczProgKopia();
        for (int x = 0; x < obraz.getWidth() - 1; x++) {
            for (int y = 0; y < obraz.getHeight() - 1; y++) {
                int pixel = obraz.getPixel(x, y);
                if (pixel > prog) {
                    obraz.putPixel(x, y, COLOR_WHITE);
                }
            }
        }
    }

    private int wyznaczProgKopia() {
        ArrayList<Double> wyniki = new ArrayList<Double>();
        double p1 = 0.0, p2 = 0.0, m1 = 0.0, m2 = 0.0, sigma1 = 0.0, sigma2 = 0.0, J = 0.0;
        int[] histogram = obraz.getHistogram();
        int liczbaPikseli = obraz.getPixelCount();

        for (int i = 1; i < 255; i++) {

            for (int j = 0; j <= i; j++) {
                p1 += ((double) histogram[j] / liczbaPikseli);
            }

            p2 = 1.0 - p1;

            for (int j = 0; j <= i; j++) {
                m1 += ((double) histogram[j] / liczbaPikseli) * j;
            }
            m1 = m1 / p1;

            for (int j = i + 1; j < 256; j++) {
                m2 += ((double) histogram[j] / liczbaPikseli) * j;
            }
            m2 = m2 / p2;

            for (int j = 0; j <= i; j++) {
                sigma1 +=  (((double) histogram[j] / liczbaPikseli) * Math.pow((double) j - m1, 2));
            }
            sigma1 = sigma1 / p1;

            for (int j = i + 1; j < 256; j++) {
                sigma2 += (Math.pow(j - m2, 2) * ((double) histogram[j] / liczbaPikseli));
            }
            sigma2 = sigma2 / p2;

            double licznik = p1 * p2 * Math.pow(m1 - m2, 2);
            double mianownik = (p1 * sigma1) + (p2 * sigma2);
            J = (p1 * p2 * Math.pow(m1 - m2, 2)) / ((p1 * sigma1) + (p2 * sigma2));
            wyniki.add(J);

            p1 = 0.0;
            p2 = 0.0;
            m1 = 0.0;
            m2 = 0.0;
            sigma1 = 0.0;
            sigma2 = 0.0;
            J = 0.0;
        }

        return max(wyniki);
    }

    private int max(ArrayList<Double> wyniki) {
        double max = Double.MIN_VALUE;
        int prog = 0;

        for (int i=0; i< wyniki.size();i++) {
            if (wyniki.get(i) > max) {
                max = wyniki.get(i);
                prog = i;
            }
        }

        return prog;
    }

    private int wyznaczProg() {
        ArrayList<Double> wartosciFunkcjiJ = new ArrayList<Double>();
        ArrayList<Double> histogramZnormalizowany = normalizujHistogram();

        for (int i = 0; i < T; i++) {
            double p1 = p1(histogramZnormalizowany, i);
            double p2 = p2(p1);
            double m1 = m1(histogramZnormalizowany, p1, i);
            double m2 = m2(histogramZnormalizowany, p2, i);
            double sigma1 = sigma1(histogramZnormalizowany, p1, m1, i);
            double sigma2 = sigma2(histogramZnormalizowany, p2, m2, i);


            double wynik = funkcjaJ(p1, p2, m1, m2, sigma1, sigma2);
            wartosciFunkcjiJ.add(wynik);
        }

        wartosciFunkcjiJ.stream().sorted((liczba1, liczba2) -> liczba1.compareTo(liczba2));

        return 15;
    }

    private ArrayList<Double> normalizujHistogram() {
        ArrayList<Double> histogramZnormalizowany = new ArrayList<Double>();
        liczbaPikseli = obraz.getPixelCount();
        int[] histogram = obraz.getHistogram();

        for (int i = 0; i < histogram.length; i++) {
            double temp = (double) histogram[i] / liczbaPikseli;
            histogramZnormalizowany.add(temp);
        }
        return histogramZnormalizowany;
    }

    private double p1(ArrayList<Double> histogram, int param) {
        double suma = 0;

        for (int i = 0; i < param; i++) {
            suma += histogram.get(i);
        }

        return suma;
    }

    private double p2(double p1) {
        return 1.0 - p1;
    }

    private double m1(ArrayList<Double> histogram, Double p1, int param) {
        Double suma = 0.0;
        for (int i = 0; i < param; i++) {
            suma += i * histogram.get(i);
        }

        if (p1 == 0.0) {
            return 0.0;
        } else {
            return suma / p1;
        }
    }

    private double m2(ArrayList<Double> histogram, Double p2, int param) {
        Double suma = 0.0;
        for (int i = param + 1; i < L; i++) {
            suma += i * histogram.get(i);
        }

        if (p2 == 0.0) {
            return 0.0;
        } else {
            return suma / p2;
        }
    }

    private double sigma1(ArrayList<Double> histogram, Double p1, double m1, int param) {
        Double suma = 0.0;
        for (int i = 0; i < param; i++) {
            double temp2 = i - m1;
            double temp = Math.pow(temp2, 2);
            suma += temp * histogram.get(i);
        }

        if (p1 == 0.0) {
            return 0.0;
        } else {
            return suma / p1;
        }
    }

    private double sigma2(ArrayList<Double> histogram, Double p2, double m2, int param) {
        Double suma = 0.0;
        for (int i = param + 1; i < L; i++) {
            double temp2 = i - m2;
            double temp = Math.pow(temp2, 2);
            suma += temp * histogram.get(i);
        }

        if (p2 == 0.0) {
            return 0.0;
        } else {
            return suma / p2;
        }
    }

    private double funkcjaJ(double p1, double p2, double m1, double m2, double sigma1, double sigma2) {
        double licznik = p1 * p2 * Math.pow(m1 - m2, 2);
        double mianownik = (p1 * sigma1) + (p2 * sigma2);

        if (mianownik != 0.0) {
            return licznik / mianownik;
        } else {
            return 0.0;
        }
    }

    private ArrayList sortuj(ArrayList<Integer> list) {
        Collections.sort(list, (liczba1, liczba2) -> liczba1.compareTo(liczba2));
        return list;
    }

    private int getColor(int pixel, int tryb) {
        return (pixel >> tryb) & 0xff;
    }

    private void doDialog() {
        GenericDialog gd = new GenericDialog("Metoda Otsu");
        gd.showDialog();
    }

    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = Lab8.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // open the Clown sample
        ImagePlus image = IJ.openImage("D:\\imageJ\\minimal-ij1-plugin-master\\src\\main\\java\\laboratorium8\\leafMonochrom.tif");
        image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}


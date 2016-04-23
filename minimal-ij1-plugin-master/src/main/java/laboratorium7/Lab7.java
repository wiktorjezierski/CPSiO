package laboratorium7;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.*;


public class Lab7 implements PlugInFilter {

    private final int R = 16;
    private final int G = 8;
    private final int B = 0;

    private final int klasa1 = 1;
    private final int klasa2 = 2;
    private final int klasa3 = 3;
    private final int klasa4 = 4;

    ImagePlus imp;
    ImageProcessor obraz;
    ImageProcessor obraz2;

    int rozmiarSasiedztwa; // podajac 3 mamy macierz 3x3
    double rzadFiltracji;  // parametr "r" z .pdf 0 <= r <= 1
    boolean minmax;
    int promien;

    Point punktSrodkowy;

    int rozmiarSubOkna[] = {0, 0, 0, 0, 0};    //indeks 0 jest nieuzywany
    int klasy[][]; //indeks oznacza piksel w macierzy, wartosc numer klasy do ktorej nalezy

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
        promien = (int) rozmiarSasiedztwa / 2;
        punktSrodkowy = new Point(promien, promien);
        klasy = new int[rozmiarSasiedztwa][rozmiarSasiedztwa];
        obraz = ip;
        obraz2 = ip;
        subOkna();
        wyswietl();
    }

    private void subOkna() {
        for (int x = 0, j = rozmiarSasiedztwa - 1; x < rozmiarSasiedztwa; x++, j--) {
            for (int y = 0; y < rozmiarSasiedztwa; y++) {
                if (x == y && x == punktSrodkowy.getX() && y == punktSrodkowy.getY()) {
                    klasy[x][y] = 0;
                } else {
                    klasy[x][y] = odlegloscPunktuOdProstej(y, j);
                }
            }
        }
    }

    private int odlegloscPunktuOdProstej(int x, int y) {
        double najkrotszaOdleglosc = Double.MAX_VALUE;
        int klasa = 0;
        x = (int) (x - punktSrodkowy.getX());
        y = (int) (y - punktSrodkowy.getY());

        double odleglosc = Math.abs(y);
        if (najkrotszaOdleglosc > odleglosc) {
            najkrotszaOdleglosc = odleglosc;
            klasa = klasa1;
        }

        odleglosc = Math.abs((-1) * x + y) / Math.sqrt(2.0);
        if (najkrotszaOdleglosc > odleglosc) {
            najkrotszaOdleglosc = odleglosc;
            klasa = klasa2;
        }

        odleglosc = Math.abs(x);
        if (najkrotszaOdleglosc > odleglosc) {
            najkrotszaOdleglosc = odleglosc;
            klasa = klasa3;
        }

        odleglosc = Math.abs(x + y) / Math.sqrt(2.0);
        if (najkrotszaOdleglosc > odleglosc) {
            najkrotszaOdleglosc = odleglosc;
            klasa = klasa4;
        }

        rozmiarSubOkna[klasa]++;

        return klasa;
    }

    private void filter() {
        for (int x = rozmiarSasiedztwa; x < obraz.getWidth() - rozmiarSasiedztwa - 1; x++) {
            for (int y = rozmiarSasiedztwa; y < obraz.getHeight() - rozmiarSasiedztwa - 1; y++) {
                obraz.putPixel(x, y, wyznaczWartoscPiksela(x, y));
            }
        }
    }

    private int wyznaczWartoscPiksela(int x, int y) {
        int wartosc = 0;
        int klasa = 0;
        for (int i = x - promien; i < x + promien; i++) {
            for (int j = y - promien; j < y + promien; j++) {
                double jasnosc[] = wyznaczJasnosc(i, j);
                double wariancja[] = wyznaczWariancje(i, j, jasnosc);
                if (minmax) {       //true = maksymalna wariancja
                    klasa = maxWariancja(wariancja);
                } else {            // minimalna wariancja
                    klasa = minWariancja(wariancja);
                }

                wartosc = zlozPiksel((int)jasnosc[klasa], (int)jasnosc[klasa + 1], (int)jasnosc[klasa + 2]);
            }
        }
        return wartosc;
    }

    private double[] wyznaczJasnosc(int x, int y) {       //zwracana wartosc RGB RGB RGB RGB  odpowiednio dla klas 1 2 3 4
        double jasnosci[] = new double[12];
        double sumaJasnosci[] = new double[12];

        for (int i = x - promien, xi = 0; i < x + promien; i++, xi++) {
            for (int j = y - promien, yi = 0; j < y + promien; j++, yi++) {
                int piksel = obraz2.getPixel(i, j);
                int R = getR(piksel);
                int G = getG(piksel);
                int B = getB(piksel);

                int numerKlasy = klasy[xi][yi];
                if (numerKlasy > 0) {
                    sumaJasnosci[(numerKlasy - 1) * 3] += R;
                    sumaJasnosci[(numerKlasy - 1) * 3 + 1] += G;
                    sumaJasnosci[(numerKlasy - 1) * 3 + 2] += B;
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            jasnosci[i] = sumaJasnosci[i] / rozmiarSubOkna[klasa1];
        }
        for (int i = 3; i < 6; i++) {
            jasnosci[i] = sumaJasnosci[i] / rozmiarSubOkna[klasa2];
        }
        for (int i = 6; i < 9; i++) {
            jasnosci[i] = sumaJasnosci[i] / rozmiarSubOkna[klasa3];
        }
        for (int i = 9; i < 12; i++) {
            jasnosci[i] = sumaJasnosci[i] / rozmiarSubOkna[klasa4];
        }
        return jasnosci;
    }

    private double[] wyznaczWariancje(int x, int y, double jasnosci[]) {
        double wariancja[] = new double[12];
        double sumaWariancji[] = new double[12];

        for (int i = x - promien, xi = 0; i < x + promien; i++, xi++) {
            for (int j = y - promien, yi = 0; j < y + promien; j++, yi++) {
                int piksel = obraz2.getPixel(i, j);
                int R = getR(piksel);
                int G = getG(piksel);
                int B = getB(piksel);

                int numerKlasy = klasy[xi][yi];
                if (numerKlasy > 0) {
                    int index = (numerKlasy - 1) * 3;
                    sumaWariancji[index] += Math.abs(R - jasnosci[index]);
                    sumaWariancji[index + 1] += Math.abs(G - jasnosci[index + 1]);
                    sumaWariancji[index + 2] += Math.abs(B - jasnosci[index + 2]);
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            wariancja[i] = sumaWariancji[i] / rozmiarSubOkna[klasa1];
        }
        for (int i = 3; i < 6; i++) {
            wariancja[i] = sumaWariancji[i] / rozmiarSubOkna[klasa2];
        }
        for (int i = 6; i < 9; i++) {
            wariancja[i] = sumaWariancji[i] / rozmiarSubOkna[klasa3];
        }
        for (int i = 9; i < 12; i++) {
            wariancja[i] = sumaWariancji[i] / rozmiarSubOkna[klasa4];
        }

        return wariancja;
    }

    private int minWariancja(double wariancja[]) {
        double minWartosc = Double.MAX_VALUE;
        int klasa = 0;
        for (int i = 0, j = 1; i < wariancja.length; i+=3, j++) {
            double temp = wariancja[i] + wariancja[i+1] + wariancja[i+2];
            if(temp < minWartosc){
                minWartosc = temp;
                klasa = j;
            }
        }
        return klasa;
    }

    private int maxWariancja(double wariancja[]) {
        double maWartosc = Double.MIN_VALUE;
        int klasa = 0;
        for (int i = 0, j = 1; i < wariancja.length; i+=3, j++) {
            double temp = wariancja[i] + wariancja[i+1] + wariancja[i+2];
            if(temp > maWartosc){
                maWartosc = temp;
                klasa = j;
            }
        }
        return klasa;
    }

    private void doDialog() {
        GenericDialog gd = new GenericDialog("Przeksztalcenie kontekstowe");
        gd.addNumericField("szerokosc sasiedztwa", 9, 0);
        gd.addNumericField("Rząd filtracjinp.: 0.5", 0.5, 0);
        gd.addCheckbox("maksymalna wariancja", true);
        gd.showDialog();

        rozmiarSasiedztwa = (int) gd.getNextNumber();
        rzadFiltracji = gd.getNextNumber();
        minmax = gd.getNextBoolean();
        if (!(rzadFiltracji <= 1 && rzadFiltracji > 0)) {
            throw new NumberFormatException("second value is not correct, must be <0,1>");
        }
    }

    private int getR(int pixel) {
        return (pixel >> R) & 0xff;
    }

    private int getG(int pixel) {
        return (pixel >> G) & 0xff;
    }

    private int getB(int pixel) {
        return (pixel >> B) & 0xff;
    }

    private int getColor(int pixel, int tryb) {
        return (pixel >> tryb) & 0xff;
    }

    private int zlozPiksel(int r, int g, int b) {
        return r << R | g << G | b;
    }

    private void wyswietl() {
        for (int x = 0; x < klasy.length; x++) {
            for (int y = 0; y < klasy.length; y++) {

                System.out.print(klasy[x][y] + " ");
            }
            System.out.println(" ");
        }
    }

    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = Lab7.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // open the Clown sample
        ImagePlus image = IJ.openImage("http://imagej.net/images/leaf.jpg");
        image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}
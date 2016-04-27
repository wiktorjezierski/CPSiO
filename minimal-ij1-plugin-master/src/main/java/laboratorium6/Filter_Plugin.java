package laboratorium6;

import java.util.ArrayList;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Filter_Plugin implements PlugInFilter {

    private final int R = 16;
    private final int G = 8;
    private final int B = 0;

    private final int r = 0;
    private final int g = 1;
    private final int b = 2;

    ImagePlus imp;
    ImageProcessor obraz;
    ImageProcessor obraz2;
    int rozmiarSasiedztwa = 0; // podajac 3 mamy macierz 3x3
    int krok;

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
        obraz = ip;
        obraz2 = ip.duplicate();
        krok = (rozmiarSasiedztwa - 1) / 2;
    }

    private void filter() {
        for (int x = krok; x < obraz.getWidth() - krok - 1; x++) {
            for (int y = krok; y < obraz.getHeight() - krok - 1; y++) {
                obraz.putPixel(x, y, wyznaczWartoscPiksela(x, y));
            }
        }
    }

    private int wyznaczWartoscPiksela(int x, int y) { // ta suma co na tablicy
        double sumaWag[] = { 0, 0, 0 };
        double suma[] = { 0, 0, 0 };
        int element = 0;
        ArrayList<Double> wagiR = wyznaczWagi(x, y, R);
        ArrayList<Double> wagiG = wyznaczWagi(x, y, G);
        ArrayList<Double> wagiB = wyznaczWagi(x, y, B);

        for (int m = x - krok; m <= x + krok; m++) {
            for (int n = y - krok; n <= y + krok; n++) {
                int piksel = obraz2.getPixel(m, n);

                double wagaR = wagiR.get(element);
                suma[r] += getR(piksel) * wagaR;
                sumaWag[r] += wagaR;

                double wagaG = wagiG.get(element);
                suma[g] += getG(piksel) * wagaG;
                sumaWag[g] += wagaG;

                double wagaB = wagiB.get(element);
                suma[b] += getB(piksel) * wagaB;
                sumaWag[b] += wagaB;

                element++;
            }
        }
        for (Double waga : sumaWag) {
            if (waga == 0) {
                waga = 1.0;
            }
        }

        int wartoscR = (int) (suma[r] / sumaWag[r]);
        int wartoscG = (int) (suma[g] / sumaWag[g]);
        int wartoscB = (int) (suma[b] / sumaWag[b]);
        return wartoscR << R  | wartoscG << G | wartoscB ;
    }

    private ArrayList<Double> wyznaczWagi(int x, int y, int tryb) { // funkcja H()
        ArrayList<Double> wagi = new ArrayList<Double>();
        ArrayList<Double> gradienty = new ArrayList<Double>();
        double sumaGradientow = 0;

        for (int m = x - krok; m <= x + krok; m++) {
            for (int n = y - krok; n <= y + krok; n++) {
                double temp = gradientOdwrotny(m, n, x, y, tryb);
                sumaGradientow += temp;
                gradienty.add(temp);
            }
        }

        for (Double gradient : gradienty) {
            double wartosc =  (0.5 * (gradient / sumaGradientow));
            wagi.add(wartosc);
        }

        int srodkowy = wagi.size() / 2;
        wagi.set(srodkowy, 0.5);

        return wagi;
    }

    private double gradientOdwrotny(int xi, int yi, int x, int y, int tryb) { // funkcja

            int pixel = (obraz2.getPixel(xi, yi) >> tryb) & 0xff;
            int pixelCentralny = (obraz2.getPixel(x, y) >> tryb) & 0xff;

            double mianownik = Math.abs(pixel - pixelCentralny);

            if (mianownik == 0) {
               return 2.0;
            }

            return 1 / mianownik;
    }

    private void doDialog() {
        GenericDialog gd = new GenericDialog("Przeksztalcenie kontekstowe");
        gd.addNumericField("szerokosc sasiedztwa", 0, 0);
        gd.showDialog();
        rozmiarSasiedztwa = (int) gd.getNextNumber();
    }

    private void checkValue(String param) {
        GenericDialog g = new GenericDialog("Przeksztalcenie kontekstowe");
        g.addMessage(param);
        g.showDialog();
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

    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
               Class<?> clazz = Filter_Plugin.class;
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

/*
            	red = (pixel>>16)&0xff;
            	green = (pixel>>8)&0xff;
            	blue = pixel&0xff; 
            	
            	pixel = (srednia << 16) | (srednia << 8 ) | srednia; 
*/


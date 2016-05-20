package laboratorium9;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Collections;


public class Lab9 implements PlugInFilter {

    private final int L = 256;
    private final int T = 255;
    private final int COLOR_WHITE = 255;
    private final int COLOR_BLACK = 0;
    private final String DESTINATION_PATH = "E:\\studia\\obrazy\\L\\lab9\\";
    private final String SUFFIX = ".tif";

    ImagePlus imp;
    ImageProcessor obraz;
    ImageProcessor obraz2;

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
        obraz = ip;
        obraz2 = ip.duplicate();
    }

    private void proguj(int prog) {
        for (int x = 0; x < obraz.getWidth() - 1; x++) {
            for (int y = 0; y < obraz.getHeight() - 1; y++) {
                int pixel = obraz.getPixel(x, y);
                if (pixel > prog) {
                    obraz2.putPixel(x, y, COLOR_WHITE);
                } else {
                    obraz2.putPixel(x, y, COLOR_BLACK);
                }
            }
        }
    }

    private void filter(){
        double progT = pierwszyPrzebieg();
        int i = 0;
        boolean warunek = true;



        while(warunek){
            obraz2 = obraz.duplicate();
            double newProgT = progowanie((int)progT, i);
            if(newProgT != progT){
                progT = newProgT;
            } else {
                warunek = false;
            }
            ++i;
        }


    }

    /*private void filter() {
        int prog = wyznaczProg();
        for (int x = 0; x < obraz.getWidth() - 1; x++) {
            for (int y = 0; y < obraz.getHeight() - 1; y++) {
                int pixel = obraz.getPixel(x, y);
                if (pixel > prog) {
                    obraz.putPixel(x, y, COLOR_WHITE);
                } else {
                    obraz.putPixel(x, y, COLOR_BLACK);
                }
            }
        }
    }*/

    private double pierwszyPrzebieg() {
        int tlo = 0;
        int pikseleTla = 0;
        int obiekt = 0;
        int pikseleObiektu = 0;
        for (int x = 0; x < obraz.getWidth(); x++) {
            for (int y = 0; y < obraz.getHeight(); y++) {
                int pixel = obraz.getPixel(x, y);
                if ((x == 0 || x == obraz2.getWidth() - 1) && (y == 0 || y == obraz2.getHeight() - 1)) {
                    tlo += pixel;
                    ++pikseleTla;
                } else {
                    obiekt += pixel;
                    ++pikseleObiektu;
                }
            }
        }
        double miB = (double) tlo / (double) pikseleTla;
        double miO = (double) obiekt / (double) pikseleObiektu;
        return ((miB + miO) / 2.0);
    }

    private double progowanie(int progT, int numer){
        int tlo = 0;
        int pikseleTla = 0;
        int obiekt = 0;
        int pikseleObiektu = 0;
        for (int x = 0; x < obraz2.getWidth(); x++) {
            for (int y = 0; y < obraz2.getHeight(); y++) {
                int pixel = obraz2.getPixel(x, y);
                if (pixel > progT ) {
                    tlo += pixel;
                    ++pikseleTla;
                    obraz2.putPixel(x,y, COLOR_WHITE);
                } else {
                    obiekt += pixel;
                    ++pikseleObiektu;
                    obraz2.putPixel(x,y, COLOR_BLACK);
                }
            }
        }

        ImagePlus image = new ImagePlus();
        image.setProcessor(obraz2);
        IJ.save(image, DESTINATION_PATH + numer + SUFFIX);

        double miB = (double) tlo / (double) pikseleTla;
        double miO = (double) obiekt / (double) pikseleObiektu;
        return ((miB + miO) / 2.0);
    }

//    private int wyznaczProg() {
//        return 0;
//    }

    private ArrayList sortuj(ArrayList<Integer> list) {
        Collections.sort(list, (liczba1, liczba2) -> liczba1.compareTo(liczba2));
        return list;
    }

    private void doDialog() {
        GenericDialog gd = new GenericDialog("Metoda Otsu");
        gd.showDialog();
    }

    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = Lab9.class;
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


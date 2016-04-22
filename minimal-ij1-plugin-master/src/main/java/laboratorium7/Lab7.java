package laboratorium7;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.ArrayList;


public class Lab7 implements PlugInFilter {

    private final int R = 16;
    private final int G = 8;
    private final int B = 0;

    private final int r = 0;
    private final int g = 1;
    private final int b = 2;

    ImagePlus imp;
    ImageProcessor obraz;
    ImageProcessor obraz2;
    int rozmiarSasiedztwa; // podajac 3 mamy macierz 3x3
    double rzadFiltracji;  // parametr "r" z .pdf 0 <= r <= 1
    boolean minmax;

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
        obraz2 = ip;
    }

    private void filter() {
        for (int x = rozmiarSasiedztwa; x < obraz.getWidth() - rozmiarSasiedztwa - 1; x++) {
            for (int y = rozmiarSasiedztwa; y < obraz.getHeight() - rozmiarSasiedztwa - 1; y++) {
                obraz.putPixel(x, y, wyznaczWartoscPiksela(x, y));
            }
        }
    }

    private int wyznaczWartoscPiksela(int x, int y) {
        return 0;
    }

    private void doDialog() {
        GenericDialog gd = new GenericDialog("Przeksztalcenie kontekstowe");
        gd.addNumericField("szerokosc sasiedztwa", 0, 0);
        gd.addNumericField("Rząd filtracjinp.: 0.5", 0.5, 0);
        gd.addCheckbox("maksymalna wariancja", true);
        gd.showDialog();

        rozmiarSasiedztwa = (int) gd.getNextNumber();
        rzadFiltracji = gd.getNextNumber();
        minmax = gd.getNextBoolean();
        if (rzadFiltracji > 1 || rzadFiltracji < 0){
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

/*
            	red = (pixel>>16)&0xff;
            	green = (pixel>>8)&0xff;
            	blue = pixel&0xff; 
            	
            	pixel = (srednia << 16) | (srednia << 8 ) | srednia; 
*/


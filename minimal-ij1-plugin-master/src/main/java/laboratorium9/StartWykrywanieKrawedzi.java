package laboratorium9;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import laboratorium10.Lab10;
import laboratorium11.Lab11;
import laboratorium8.Lab8;

import java.util.Vector;


public class StartWykrywanieKrawedzi {

    private static final String OTSU = "Otsu";
    private static final String PROGOWANIE_ITERACYJNE = "Progowanie iteracyjne";
    private static final String PROGOWANIE_Z_HISTEREZA = "Progowanie z histereza";
    private static final String ROZPOZNAWANIE = "Rozpoznawanie";


    public static void main(String[] args) {
        // start ImageJ
        new ImageJ();

        GenericDialog gd = new GenericDialog("Przeksztalcenie kontekstowe");
        String[] tab = {OTSU, PROGOWANIE_ITERACYJNE, PROGOWANIE_Z_HISTEREZA, ROZPOZNAWANIE};
        gd.addRadioButtonGroup("wybor algorytmu", tab, 3, 1, null);
        gd.showDialog();

        String wybor = gd.getNextRadioButton();

        if (wybor.equals("null")) {
            System.exit(0);
        }

        Class<?> clazz = null;
        switch (wybor) {
            case OTSU:
                    clazz = Lab8.class;
                    break;
            case PROGOWANIE_ITERACYJNE:
                    clazz = Lab9.class;
                    break;
            case PROGOWANIE_Z_HISTEREZA:
                    clazz = Lab10.class;
                    break;
            case ROZPOZNAWANIE:
                    clazz = Lab11.class;
                    break;
        }

        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // open the image
        ImagePlus image = IJ.openImage("E:\\studia\\obrazy\\L\\lab11\\00000.tif");
        image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}

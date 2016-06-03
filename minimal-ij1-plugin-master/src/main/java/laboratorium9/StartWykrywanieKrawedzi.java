package laboratorium9;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import laboratorium10.Lab10;
import laboratorium8.Lab8;

import java.util.Vector;


public class StartWykrywanieKrawedzi {

    private static final String OTSU = "Otsu";
    private static final String PROGOWANIE_ITERACYJNE = "Progowanie iteracyjne";
    private static final String PROGOWANIE_Z_HISTEREZA = "Progowanie z histereza";


    public static void main(String[] args) {
        // start ImageJ
        new ImageJ();

        GenericDialog gd = new GenericDialog("Przeksztalcenie kontekstowe");
        String[] tab = {OTSU, PROGOWANIE_ITERACYJNE, PROGOWANIE_Z_HISTEREZA};
        gd.addRadioButtonGroup("wybor algorytmu", tab, 3, 1, null);
        gd.showDialog();

        String wybor = gd.getNextRadioButton();

        if (wybor.equals("null")) {
            System.exit(0);
        }

        Class<?> clazz = null;
        if (wybor.equals(OTSU)) {
            clazz = Lab8.class;
        } else if (wybor.equals(PROGOWANIE_ITERACYJNE)) {
            clazz = Lab9.class;
        } else if (wybor.equals(PROGOWANIE_Z_HISTEREZA)) {
            clazz = Lab10.class;
        }

        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // open the image
//        ImagePlus image = IJ.openImage("D:\\imageJ\\minimal-ij1-plugin-master\\src\\main\\java\\laboratorium8\\leafMonochrom.tif");
//        ImagePlus image = IJ.openImage("D:\\imageJ\\minimal-ij1-plugin-master\\src\\main\\java\\laboratorium10\\leafH.tif");
        ImagePlus image = IJ.openImage("E:\\studia\\obrazy\\L\\lab11\\000001.bmp");
        image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}

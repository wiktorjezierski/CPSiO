package laboratorium9;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import laboratorium8.Lab8;

import java.util.Vector;


public class StartWykrywanieKrawedzi {

    private static final String OTSU = "Otsu";
    private static final String PROGOWANIE_ITERACYJNE = "Progowanie iteracyjne";


    public static void main(String[] args) {
        // start ImageJ
        new ImageJ();

        GenericDialog gd = new GenericDialog("Przeksztalcenie kontekstowe");
        String[] tab = {OTSU,PROGOWANIE_ITERACYJNE};
        gd.addRadioButtonGroup("wybor algorytmu",tab,2,1,null);
        gd.showDialog();

        String wybor = gd.getNextRadioButton();

        if(wybor.equals("null")){
            System.exit(0);
        }

        Class<?> clazz = null;
        if (wybor.equals(OTSU)){
            clazz = Lab8.class;
        } else if (wybor.equals(PROGOWANIE_ITERACYJNE)){
            clazz = Lab9.class;
        }

        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // open the image
        ImagePlus image = IJ.openImage("D:\\imageJ\\minimal-ij1-plugin-master\\src\\main\\java\\laboratorium8\\leafMonochrom.tif");
        image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}

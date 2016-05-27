package laboratorium10;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import laboratorium8.Lab8;

public class Lab10 implements PlugInFilter {

    private final int COLOR_WHITE = 255;
    private final int COLOR_BLACK = 0;
    private final String DESTINATION_PATH = "E:\\studia\\obrazy\\L\\lab9\\";
    private final String SUFFIX = ".tif";

    ImagePlus imp;
    ImageProcessor obraz;
    ImageProcessor obraz2;
    ImageProcessor imageOTSU;

    double liczbaPikseli;
    double K = 1.0;
    int prog;               //T na tablicy
    boolean histereza = false;

    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL;
    }

    public void run(ImageProcessor ip) {
        prepareImage(ip);
        filter();
    }

    private void prepareImage(ImageProcessor ip) {
        obraz = ip;
        obraz2 = ip.duplicate();
        doDialog();
    }

    private void filter() {
        Lab8 lab8 = new Lab8();
        imageOTSU = lab8.runOtsu(obraz2);
        prog = lab8.getProg();

        saveToFile(imageOTSU, "test");

        if(!histereza){
            obraz.setPixels(imageOTSU.getPixels());
            return;
        }

        for (int x = 1; x < obraz.getWidth(); x++) {
            for (int y = 1; y < obraz.getHeight(); y++) {
                if(imageOTSU.getPixel(x, y) < (prog + sigma(x, y) * K)){
                    obraz.putPixel(x, y, COLOR_BLACK);
                } else {
                    obraz.putPixel(x, y, COLOR_WHITE);
                }
            }
        }
    }

    public int sigma(int x, int y) {
        int piksel1 = imageOTSU.getPixel(x - 1, y);
        int piksel2 = imageOTSU.getPixel(x - 1, y - 1);
        int piksel3 = imageOTSU.getPixel(x, y - 1);

        if ((piksel1 + piksel2 + piksel3) < 2 * 255){
            return 1;
        } else {
            return -1;
        }
    }

    private void doDialog(){
        GenericDialog gd = new GenericDialog("Przeksztalcenie kontekstowe");
        gd.addCheckbox("histereza", false);
        gd.addNumericField("Parametr K ", 0.0, 3);
        gd.showDialog();

        histereza = gd.getNextBoolean();
        K = gd.getNextNumber();
    }

    public void saveToFile(ImageProcessor obraz, String name){
        ImagePlus image = new ImagePlus();
        image.setProcessor(obraz);
        IJ.save(image, DESTINATION_PATH + name + SUFFIX);
    }

    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = Lab10.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // open the Clown sample
        ImagePlus image = IJ.openImage("D:\\imageJ\\minimal-ij1-plugin-master\\src\\main\\java\\laboratorium10\\leafH.tif");
        image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}

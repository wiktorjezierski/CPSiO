package laboratorium11;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import laboratorium8.Lab8;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


public class Lab11 implements PlugInFilter {

    private final static String SOURCE_PATH = "E:\\studia\\obrazy\\L\\lab11\\";
    private final static String SUFFIX = ".tif";
    private final static String DESTINATION_PATH = "E:\\studia\\obrazy\\L\\test\\";
    private final static String RESULT_FILE = "E:\\studia\\obrazy\\L\\lab11\\wynik.txt";

    ImagePlus imp;
    ImageProcessor obraz;
    ImageProcessor obraz2;

    private MyParticleAnalyzer analyzer;
    private File file;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        this.imp = imagePlus;
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        prepareImage(ip);
        filter();
    }

    private void prepareImage(ImageProcessor ip) {
        obraz = ip;
        obraz2 = ip.duplicate();
        analyzer = new MyParticleAnalyzer();

        try {
            file = new File(RESULT_FILE);
            fileWriter = new FileWriter(file.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fileWriter);

            if (!file.exists()) {
                file.createNewFile();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void filter() {
        Lab8 lab8 = new Lab8();

        for (int i = 0; i < 60; i++) {
            ImagePlus image = IJ.openImage(SOURCE_PATH + getName(i) + SUFFIX);
            ImageProcessor imageOTSU = lab8.runOtsu(image.getProcessor());
            saveToFile(imageOTSU, getName(i));
            obraz.setPixels(imageOTSU.getPixels());
            createStatistic(imageOTSU);
        }
    }

    private void createStatistic(ImageProcessor otsu) {
        ImagePlus imagePlus = new ImagePlus();
        imagePlus.setProcessor(otsu);
        boolean analyze = analyzer.analyze(imagePlus);
        ResultsTable resultsTable = analyzer.getResultsTable();
    }

    private String getName(int i) {
        String nazwa = Integer.toString(i * 40);
        while (nazwa.length() < 5) {
            nazwa = "0" + nazwa;
        }
        return nazwa;
    }

    public void saveToFile(ImageProcessor obraz, String name) {
        ImagePlus image = new ImagePlus();
        image.setProcessor(obraz);
        IJ.save(image, DESTINATION_PATH + name + SUFFIX);
    }

    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = Lab11.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // open the Clown sample
        ImagePlus image = IJ.openImage("E:\\studia\\obrazy\\L\\lab11\\00000.tif");
        image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}

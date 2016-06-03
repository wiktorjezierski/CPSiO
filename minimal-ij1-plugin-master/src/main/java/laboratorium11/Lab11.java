package laboratorium11;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import laboratorium8.Lab8;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Lab11 implements PlugInFilter, Measurements {

    private final static String SOURCE_PATH = "E:\\studia\\obrazy\\L\\lab11\\";
    private final static String SUFFIX = ".tif";
    private final static String DESTINATION_PATH = "E:\\studia\\obrazy\\L\\test\\";
    private final static String RESULT_FILE = "E:\\studia\\obrazy\\L\\lab11\\wynik.txt";

    ImagePlus imp;
    ImageProcessor obraz;
    ImageProcessor obraz2;

    private ParticleAnalyzer analyzer;
    private ParticleAnalyzer analyzer2;
    private File file;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    private ResultsTable rt;

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

        int options = ParticleAnalyzer.SHOW_PROGRESS;
        int minSize = 1;
        int maxSize = Integer.MAX_VALUE;
        rt = new ResultsTable();
        analyzer = new ParticleAnalyzer(options, CIRCULARITY, rt, minSize, maxSize);
        analyzer2 = new ParticleAnalyzer(options, AREA, rt, minSize, maxSize);

        try {
            file = new File(RESULT_FILE);
            fileWriter = new FileWriter(file.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fileWriter);

            if (file.exists()) {
                file.delete();
                file.createNewFile();
            } else {
                file.createNewFile();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void filter() {
        Lab8 lab8 = new Lab8();

        for (int i = 0; i < 60; i++) {

            String name = getName(i);
            ImagePlus image = IJ.openImage(SOURCE_PATH + name + SUFFIX);
            ImageProcessor imageOTSU = lab8.runOtsu(image.getProcessor());
            saveToFile(imageOTSU, name);
            obraz.setPixels(imageOTSU.getPixels());
            createStatisticAndSaveToFile(imageOTSU, name);
        }


        try {
            bufferedWriter.close();
        } catch (Exception e){

        }
    }

    private void createStatisticAndSaveToFile(ImageProcessor otsu, String name) {
        ImagePlus imagePlus = new ImagePlus();
        otsu.setThreshold(255, 255, ImageProcessor.NO_LUT_UPDATE);
        imagePlus.setProcessor(otsu);
        analyzer.analyze(imagePlus);
        analyzer2.analyze(imagePlus);
        float[] area = rt.getColumn(ResultsTable.AREA);
        float[] circle = rt.getColumn(ResultsTable.CIRCULARITY);

        try{
            bufferedWriter.write("Plik " + name + ": \n");
            for (int i = 0; i < area.length; i++) {
                bufferedWriter.write("Pole = " + area[i] + " Obwód = " + circle[i] + "\n");
            }
            bufferedWriter.write("\n");
        } catch (IOException e){
            e.printStackTrace();
        }
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

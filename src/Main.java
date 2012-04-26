
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Main {

  public static void main(String[] args) throws Exception {
    if (getBenchmarkFile() == null)
      processImage();
    else
      runBenchmark();
  }

  private static void runBenchmark() throws Exception {
    int times = 10;
    int threads = Runtime.getRuntime().availableProcessors();
    long[][] data = new long[threads][times];
    long totalTime = 0;
    Image img = readImage(getInfile());

    for (int k = 0; k < threads; k++ )
    {
      for (int j = 0; j < times; j++)
      {
        ImageProcessor imageProcessor = new ImageProcessor(img, (k+1), getPhases());
        totalTime += data[k][j] = imageProcessor.process();
      }
    }
    System.out.println("Benchmark took " + totalTime + " ms");
    writeBenchmarkResults(average(data));
  }

  private static void processImage() throws Exception {
    Image img = readImage(getInfile());
    ImageProcessor imageProcessor =
            new ImageProcessor(img, getThreads(), getPhases());
    imageProcessor.process();
    img.Save(getOutfile());
  }

  private static Image readImage(String filename) {
    Image img = null;
    long t_start = System.nanoTime();
    try {
      img = new Image(filename);
    } catch (Exception ex) {
      System.out.println("Unable to read the image file.");
      System.out.println(ex);
      System.exit(0);
    }
    long t_end = System.nanoTime();
    System.out.println("Image loaded in " + (t_end - t_start) / 1000000 + " ms");
    return img;
  }

  private static String getBenchmarkFile() {
    return System.getProperty("benchmarkFile");
  }

  private static String getInfile() {
    String infile = System.getProperty("infile");
    System.out.println("infile: " + infile);
    return infile;
  }

  private static String getOutfile() {
    String outfile = System.getProperty("outfile");
    System.out.println("outfile: " + outfile);
    return outfile;
  }

  private static int getThreads() {
    int threads = 0;
    try {
      threads = Integer.parseInt(System.getProperty("threads"));
    } catch (NumberFormatException e) {
      System.out.println("threads needs to be a number");
      System.exit(0);
    }
    if (threads < 0)
      threads = Runtime.getRuntime().availableProcessors();
    System.out.println("threads: " + threads);
    return threads;
  }

  private static int getPhases() {
    int phases = 0;
    try {
      phases = Integer.parseInt(System.getProperty("phases"));
    } catch (NumberFormatException e) {
      System.out.println("phases needs to be a number");
      System.exit(0);
    }
    System.out.println("phases: " + phases);
    return phases;
  }

  private static long[] average(long[][] data) {
    long[] average = new long[data.length];
    for (int i = 0; i< data.length; i++) {
      long sum = 0;
      for (int j = 0; j< data[0].length; j++) {
        sum += data[i][j];
      }
      average[i] = sum / data[0].length;
    }
    return average;
  }

  private static void writeBenchmarkResults(long[] results) throws Exception {
    System.out.println("Writing benchmark results to " + getBenchmarkFile());
    File f = new File(getBenchmarkFile());
    FileWriter fw = new FileWriter(f);
    BufferedWriter bw = new BufferedWriter(fw);
    
    bw.write("[");
    for (int i=0; i < results.length; i++) {
      bw.write("["+(i+1)+","+results[i]+"]");
      if (i+1 != results.length)
        bw.write(",");
    }
    bw.write("]\n");

    bw.close();
    fw.close();
  }
}

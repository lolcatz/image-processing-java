
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

  /*
   * If a benchmark file exists, run benchmark tests.
   * Otherwise run image processing. Benchmarking 
   * works with start_benchmark shellscript.
   */
  public static void main(String[] args) throws Exception {
    System.out.println("Doing operations: " + Arrays.toString(getOperations()));
    if (getBenchmarkFile() == null)
      processImage();
    else
      runBenchmark();
  }

  /*
   * Method for counting benchmark times.
   */
  private static void runBenchmark() throws Exception {
    int times = getTestTimes();
    int threads = getThreads();
    long[][] data = new long[threads][times];
    long totalTime = 0;
    Image img = readImage(getInfile());
    
    /*
     * Counts the running time for 0 to k threads, where k is defined 
     * in start_benchmark shellscript.
     */
    for (int k = 0; k < threads; k++ )
    {
      System.out.println("Benchmarking with " + (k+1) + " threads");
      for (int j = 0; j < times; j++)
      {
        System.out.println("Run " + (k*times + j+1)+ "/" + (threads*times));
        ImageProcessor imageProcessor = new ImageProcessor(img, k+1, getOperations());
        totalTime += data[k][j] = imageProcessor.process();
      }
    }
    System.out.println("Benchmark took " + totalTime + " ms");
    /*
     * Writes the benchmark results to the benchmark file also defined in the shellscript.
     */
    writeBenchmarkResults(average(data));
  }

  
  private static void processImage() throws Exception {
    Image img = readImage(getInfile());
    ImageProcessor imageProcessor =
            new ImageProcessor(img, getThreads(), getOperations());
    imageProcessor.process();
    saveImage(img, getOutfile());
  }

  public static void saveImage(Image img, String filename) {
    System.out.println("Saving image to " + filename);

    long t_start = System.nanoTime();
    try {
      img.save(filename);
    } catch (Exception ex) {
      System.out.println("Saving image failed.");
      System.out.println(ex);
    }
    long t_end = System.nanoTime();
    System.out.println("Image saved in " + (t_end - t_start) / 1000000 + " ms");
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

  /*
   * Reads the image from the shellscript.
   */
  private static String getInfile() {
    String infile = System.getProperty("infile");
    System.out.println("Input file: " + infile);
    return infile;
  }

  /*
   * Reads the desired output file from the shellcript.
   */
  private static String getOutfile() {
    String outfile = System.getProperty("outfile");
    System.out.println("Output file: " + outfile);
    return outfile;
  }
  /*
   * Reads the amount of test times from the shellcript.
   */
  private static int getTestTimes() {
    int times = 10;
    try {
      times = Integer.parseInt(System.getProperty("testTimes"));
    } catch (NumberFormatException e) {
      System.out.println("testTimes not specified, defaulting to " + times);
    }
    return times;

  }
  /*
   * Method for adjusting the amount of threads available. Read from the shellscript 
   * unless the value is -1. For the negative values the amount available threads is asked from 
   * Runtime.
   */
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
    System.out.println("Threads: " + threads);
    return threads;
  }
  /*
   * Method for deciding which operations are run for the image. Operations are 
   * smoothen, A, and sharpen, B. Defined in the shellscripts.
   */
  private static Operation[] getOperations() {
    ArrayList<Operation> operations = new ArrayList<Operation>();
    String operationsString = System.getProperty("operations");
    if (operationsString == null) {
      System.out.println("No operations specified, exiting.");
      System.exit(0);
    }

    for (char c : operationsString.toCharArray()) {
      if (c == 'A')
        operations.add(Operation.Smoothen);
      else if (c == 'B')
        operations.add(Operation.Sharpen);
      else
        System.out.println("Invalid operation '" + c + "', ignoring.");
    }

    return operations.toArray(new Operation[operations.size()]);
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
  
  /*
   * Method for writing the benchmark results for the benchmark tests. The 
   * output file is defined in the shellscripts benchmarkFile.
   */
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

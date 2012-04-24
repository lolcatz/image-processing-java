public class Main {


  public static void main(String[] args) throws Exception {
    if (!getTest()) {
      ProcessImage main = new ProcessImage(getInfile(), getOutfile(), getThreads(), getPhases());
      main.processImage();
      main.saveImage();
    } else {
      int i = Runtime.getRuntime().availableProcessors();
      for (int k=1; k <= i; k++ ) {
        ProcessImage main = new ProcessImage(getInfile(), getOutfile(), k, getPhases());
        main.processImage();
      }
    }
  }

  private static boolean getTest() {
    return System.getProperty("test") != null;
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
}

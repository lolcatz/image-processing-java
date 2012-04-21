import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

  private final String infile;
  private final String outfile;
  private final Image helperImage;
  private final Image image;

  private static void usage() {
    System.out.print("Usage: java -jar dist/image_processing.jar ");
    System.out.println("<input file> [output file]");
    System.out.println("Input file is mandatory, output file is not.");
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      usage();
      System.exit(0);
    }

    String infile = args[0];
    String outfile = null;

    System.out.println("Input file: " + infile);

    if (args.length >= 2) {
      outfile = args[1];
      System.out.println("Output file: " + outfile);
    } else {
      System.out.println("Output won't be saved.");
    }

    Main main = new Main(infile, outfile);
  }

  private Main(String infile, String outfile) throws Exception {
    this.infile = infile;
    this.outfile = outfile;
    this.image = readImage();
    this.helperImage = new Image(this.image.width,
            this.image.height, this.image.maxval);

    long t_start = System.nanoTime();
    // set number of threads and repetitions for operations
    final int threads = 4;
    final int phases = 5;

    System.out.println("Processing with "+threads+" threads..");

    // initialize semaphores for synchronization
    Semaphore stop = new Semaphore(0);
    Semaphore[] gates = new Semaphore[threads];
    for(int i = 0; i < threads; i++)
      gates[i] = new Semaphore(1);

    // create barrier thread for sync
    Barrier b = new Barrier(stop, gates, threads, phases, this.helperImage, this.image);
    Thread barrierThread = new Thread(b);

    // initialize worker threads for smoothen, assign each a slice of the image
    Thread[] ts = new Thread[threads];
    for (int i = 0; i < threads; i++)
      ts[i] = new Thread(new ImageWorker(stop, gates[i], phases,
              this.image, this.helperImage,
              (i*this.image.height)/threads,
              (i+1)*this.image.height/threads));

    // start worker and barrier threads
    for (int i = 0; i < threads; i++)
      ts[i].start();
    barrierThread.start();

    // wait while work still ongoing
    while (!b.finished) {Thread.sleep(10);}

    // end timing, output time
    long t_end = System.nanoTime();
    System.out.println("Time: "+(t_end - t_start)/1000000 + " ms");

    if (outfile != null) {
      // save processed image
      t_start = System.nanoTime();
      try {
        this.image.Save(outfile);
      } catch (Exception ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      }
      t_end = System.nanoTime();

      System.out.println("Image saved in: "+(t_end - t_start)/1000000 + " ms");
    }
  }

  private Image readImage() {
    Image img = null;
    long t_start = System.nanoTime();
    try {
      img = new Image(infile);
    } catch (Exception ex) {
      System.out.println("Unable to read the image file.");
      System.out.println(ex);
      System.exit(0);
    }
    long t_end = System.nanoTime();
    System.out.println("Image loaded in " +(t_end - t_start)/1000000 + " ms");
    return img;
  }

}

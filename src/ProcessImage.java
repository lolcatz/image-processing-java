import java.util.concurrent.Semaphore;


public class ProcessImage {
  private String infile;
  private String outfile;
  private int threads;
  private int phases;

  private Image helperImage;
  private Image image;

  public ProcessImage(String infile, String outfile, int threads, int phases) throws Exception {
    this.infile = infile;
    this.outfile = outfile;
    this.threads = threads;
    this.phases = phases;
  }

  private void verifyResult() {

  }

  public void processImage() throws Exception {
    this.image = readImage();
    this.helperImage = new Image(this.image.width,
            this.image.height, this.image.maxval);

    long t_start = System.nanoTime();
    System.out.println("Processing with " + threads + " threads..");

    // initialize semaphores for synchronization
    Semaphore stop = new Semaphore(0);
    Semaphore finished = new Semaphore(0);
    Semaphore[] gates = new Semaphore[threads];
    for (int i = 0; i < threads; i++)
      gates[i] = new Semaphore(1);

    // create barrier thread for sync
    Barrier b = new Barrier(finished, stop, gates, threads, phases, this.helperImage, this.image);
    Thread barrierThread = new Thread(b);

    // initialize worker threads for smoothen, assign each a slice of the image
    Thread[] ts = new Thread[threads];
    for (int i = 0; i < threads; i++)
      ts[i] = new Thread(new ImageWorker(stop, gates[i], phases,
              this.image, this.helperImage,
              (i * this.image.height) / threads,
              (i + 1) * this.image.height / threads));

    // start worker and barrier threads
    for (int i = 0; i < threads; i++)
      ts[i].start();
    barrierThread.start();

    // wait while work still ongoing
    try {
      finished.acquire();
    } catch (Exception e) {
    }

    // end timing, output time
    long t_end = System.nanoTime();
    System.out.println("Time: " + (t_end - t_start) / 1000000 + " ms");

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
    System.out.println("Image loaded in " + (t_end - t_start) / 1000000 + " ms");
    return img;
  }

  public void saveImage() {
    if (outfile == null) {
      System.out.println("output file not specified, not saving.");
      return;
    }

    System.out.println("Saving output to " + this.outfile);

    long t_start = System.nanoTime();
    try {
      this.image.Save(outfile);
    } catch (Exception ex) {
      System.out.println("Saving image failed.");
      System.out.println(ex);
    }
    long t_end = System.nanoTime();

    System.out.println("Image saved in " + (t_end - t_start) / 1000000 + " ms");
  }
}

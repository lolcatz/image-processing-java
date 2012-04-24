import java.util.concurrent.Semaphore;

public class Main {

  private String infile;
  private String outfile;
  private int threads;
  private int phases;

  private Image helperImage;
  private Image image;

  public static void main(String[] args) throws Exception {
    Main main = new Main();
    main.processImage();
    main.saveImage();
  }

  private Main() throws Exception {
    setProperties();
  }

  private void verifyResult() {
    
  }

  private void processImage() throws Exception {
    this.image = readImage();
    this.helperImage = new Image(this.image.width,
            this.image.height, this.image.maxval);

    long t_start = System.nanoTime();
    System.out.println("Processing with " + threads + " threads..");

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

  }

  private void setProperties() {
    this.infile = System.getProperty("infile");
    System.out.println("infile: " + this.infile);

    this.outfile = System.getProperty("outfile");
    System.out.println("outfile: " + this.outfile);

    try {
      this.phases = Integer.parseInt(System.getProperty("phases"));
    } catch (NumberFormatException e) {
      System.out.println("phases needs to be a number");
      System.exit(0);
    }
    System.out.println("phases: " + this.phases);

    try {
      this.threads = Integer.parseInt(System.getProperty("threads"));
    } catch (NumberFormatException e) {
      System.out.println("threads needs to be a number");
      System.exit(0);
    }
    System.out.println("threads: " + this.threads);
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

  private void saveImage() {
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

    System.out.println("Image saved in "+(t_end - t_start)/1000000 + " ms");
  }

}

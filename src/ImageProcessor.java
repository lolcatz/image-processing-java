import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class ImageProcessor {
  private int threads;
  private Operation[] operations;

  private Image helperImage;
  private Image image;

  public ImageProcessor(Image image, int threads, Operation[] operations) throws Exception {
    this.image = image;
    this.threads = threads;
    this.operations = operations;
  }

  public long process() throws Exception {
    this.helperImage = new Image(this.image.width,
            this.image.height, this.image.maxval);

    long t_start = System.nanoTime();

    // initialize semaphores for synchronization
    Semaphore stop = new Semaphore(0);
    Semaphore finished = new Semaphore(0);
    Semaphore[] gates = new Semaphore[threads];
    for (int i = 0; i < threads; i++)
      gates[i] = new Semaphore(1);

    // create barrier thread for sync
    Barrier b = new Barrier(finished, stop, gates, threads, this.operations.length);
    Thread barrierThread = new Thread(b);

    // initialize worker threads for smoothen, assign each a slice of the image
    Thread[] ts = new Thread[threads];
    for (int i = 0; i < threads; i++)
      ts[i] = new Thread(new ImageWorker(stop, gates[i], this.operations,
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
    long time = (t_end - t_start) / 1000000;
    System.out.println("Time: " + time + " ms");
    return time;
  }

  public void saveImage(String filename) {
    System.out.println("Saving image to " + filename);

    long t_start = System.nanoTime();
    try {
      this.image.save(filename);
    } catch (Exception ex) {
      System.out.println("Saving image failed.");
      System.out.println(ex);
    }
    long t_end = System.nanoTime();
    System.out.println("Image saved in " + (t_end - t_start) / 1000000 + " ms");
  }
}

import java.util.concurrent.Semaphore;

public class ImageWorker implements Runnable {
  Semaphore stop;
  Semaphore gate;
  Operation[] operations;

  Image im;
  Image helper;
  int row_start;
  int row_end;

  public ImageWorker(Semaphore stop, Semaphore gate, Operation[] operations,
      Image im, Image helper, int row_start, int row_end) {
    this.stop = stop;
    this.gate = gate;
    this.operations = operations;
    this.im = im;
    this.helper = helper;
    this.row_start = row_start;
    this.row_end = row_end;
  }

  public void run() {
    // first smoothen
    for (Operation op : this.operations) {
      // wait for gate
      try { gate.acquire(); } catch (Exception e) {}
      // process a slice of the image
      if (op == Operation.Smoothen)
        helper = Image.Smoothen(helper, im, row_start, row_end);
      else if (op == Operation.Sharpen)
        helper = Image.Sharpen(helper, im, row_start, row_end);
      // swap references for next round
      Image temp = helper;
      helper = im;
      im = temp;
      // indicate work done to barrier thread
      stop.release();
    }
  }
}

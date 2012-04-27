import java.util.concurrent.Semaphore;

public class ImageWorker implements Runnable {
  private Semaphore stop;
  private Semaphore gate;
  private Operation[] operations;

  private Image im;
  private Image helper;
  private int row_start;
  private int row_end;

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
    for (Operation op : this.operations) {
      // wait for gate
      try { gate.acquire(); } catch (Exception e) {}
      // indicate work done to barrier thread
      doOperation(op);
      stop.release();
    }
  }

  private void doOperation(Operation op) {
    // process a slice of the image
    if (op == Operation.Smoothen)
      this.helper = Image.smoothen(helper, im, row_start, row_end);
    else if (op == Operation.Sharpen)
      this.helper = Image.sharpen(helper, im, row_start, row_end);
    // swap references for next round
    Image temp = this.helper;
    this.helper = this.im;
    this.im = temp;
  }
}

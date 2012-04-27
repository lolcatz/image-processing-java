import java.util.concurrent.Semaphore;

public class Barrier implements Runnable {
  private Semaphore stop;
  private Semaphore[] gates;
  private Semaphore finished;

  private int operations;
  private int threads;

  public Barrier(Semaphore finished, Semaphore stop, Semaphore[] gates, int threads, int operations) {
    this.stop = stop;
    this.gates = gates;
    this.threads = threads;
    this.operations = operations;
    this.finished = finished;
  }

  public void run() {
    for (int o = 0; o < this.operations; ++o) {
      // wait for all of the threads to stop
      for (int i = 0; i < threads; i++)
        try { stop.acquire(); } catch (Exception e) {}
      // release all worker threads
      for (int i = 0; i < threads; i++)
        gates[i].release();
    }
    finished.release();
  }
}

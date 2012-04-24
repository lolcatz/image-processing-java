/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.concurrent.Semaphore;

/**
 *
 * @author Paul
 */

public class Barrier implements Runnable {
  Semaphore stop;
  Semaphore[] gates;
  Semaphore finished;
  int threads;
  int phases;
  Image helper;
  Image im;

  public Barrier(Semaphore finished, Semaphore stop, Semaphore[] gates, int threads, int phases,
            Image im, Image helper) {
    this.stop = stop;
    this.gates = gates;
    this.threads = threads;
    this.phases = phases;
    this.im = im;
    this.helper = helper;
    this.finished = finished;
  }

  public void run() {
    for (int j = 0; j < phases*2; j++) {
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

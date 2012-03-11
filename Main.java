/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.concurrent.Semaphore;

/**
 *
 * @author Paul
 */
public class Main {
  static final String filename = "test.ppm";

  public static void main(String[] args) throws Exception{
    // read the image file and initialize a helper image of the same size
    long t_start = System.nanoTime();
    Image im = new Image(filename);
    long t_end = System.nanoTime();
    System.out.println("Image loaded in " +(t_end - t_start)/1000000 + " ms");

    t_start = System.nanoTime();
    // set number of threads and repetitions for operations
    final int threads = 4;
    final int phases = 5;

    System.out.println("Processing with "+threads+" threads..");

    // create helper image
    Image helper = new Image(im.width, im.height, im.maxval);

    // initialize semaphores for synchronization
    Semaphore stop = new Semaphore(0);
    Semaphore[] gates = new Semaphore[threads];
    for(int i = 0; i < threads; i++)
      gates[i] = new Semaphore(1);

    // create barrier thread for sync
    Barrier b = new Barrier(stop, gates, threads, phases, helper, im);
    Thread barrierThread = new Thread(b);
    // initialize worker threads for smoothen, assign each a slice of the image
    Thread[] ts = new Thread[threads];
    for (int i = 0; i < threads; i++)
      ts[i] = new Thread(new ImageWorker(stop, gates[i], phases, im, helper,
                         (i*im.height)/threads, ((i+1)*im.height)/threads));

    // start worker and barrier threads
    for (int i = 0; i < threads; i++)
      ts[i].start();
    barrierThread.start();

    // wait while work still ongoing
    while (!b.finished) {Thread.sleep(10);}

    // end timing, output time
    t_end = System.nanoTime();
    System.out.println("Time: "+(t_end - t_start)/1000000 + " ms");

    // save processed image
    t_start = System.nanoTime();
    im.Save("processed.ppm");
    t_end = System.nanoTime();

    System.out.println("Image saved in: "+(t_end - t_start)/1000000 + " ms");
  }
}

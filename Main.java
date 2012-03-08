/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rio_image_processing;

import java.util.concurrent.Semaphore;

/**
 *
 * @author Paul
 */
public class Main {
    static final String filename = "test.ppm";
    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) throws Exception{
        // read the image file and initialize a helper image of the same size
        long t_start = System.nanoTime();
        Image im = new Image(filename);
        Image helper = new Image(im.width, im.height, im.maxval);
        long t_end = System.nanoTime();
        System.out.println("initializing: " +(t_end - t_start)/1000000 + " ms");


        /*t_start = System.nanoTime();
        for (int i = 0; i < 5; i++) {

            helper = Image.Smoothen(helper, im, 0, im.height);
            Image temp = im;
            im = helper;
            helper = temp;
        }
        for (int i = 0; i < 5; i++) {

            helper = Image.Sharpen(helper, im, 0, im.height);
            Image temp = im;
            im = helper;
            helper = temp;
        }
        t_end = System.nanoTime();
        System.out.println("Single thread: "+(t_end - t_start)/1000000 + " ms");
        im.Save("smoothed0.ppm");*/
        // start timing



        t_start = System.nanoTime();
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
        Barrier b = new Barrier(stop, gates, threads, phases, helper, im);
        Thread barrierThread = new Thread(b);
        // initialize worker threads for smoothen, assign each a slice of the image
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++)
            ts[i] = new Thread(new SmoothenWorker(stop, gates[i], phases, im, helper,
                    (i*im.height)/threads, ((i+1)*im.height)/threads));

        // start worker and barrier threads
        for (int i = 0; i < threads; i++)
            ts[i].start();
        barrierThread.start();

        // wait while work still ongoing
        while (!b.finished) {Thread.sleep(10);}

        t_end = System.nanoTime();
        System.out.println("Smoothing: "+(t_end - t_start)/1000000 + " ms");
        im.Save("smoothed.ppm");

        t_start = System.nanoTime();

        // repeat all of the above for sharpen operations
        b = new Barrier(stop, gates, threads, phases, helper, im);
        barrierThread = new Thread(b);

        ts = new Thread[threads];
        for (int i = 0; i < threads; i++)
            ts[i] = new Thread(new SharpenWorker(stop, gates[i], phases, im, helper,
                    (i*im.height)/threads, ((i+1)*im.height)/threads));

        for (int i = 0; i < threads; i++)
            ts[i].start();
        barrierThread.start();

        while (!b.finished) {Thread.sleep(10);}

        // end timing, output time
        t_end = System.nanoTime();
        System.out.println("Sharpening: "+(t_end - t_start)/1000000 + " ms");

        // save processed image
        im.Save("sharpened.ppm");
    }

    private static class Barrier implements Runnable {
        Semaphore stop;
        Semaphore[] gates;
        int threads;
        int phases;
        Image helper;
        Image im;

        boolean finished = false;
        public Barrier(Semaphore stop, Semaphore[] gates, int threads, int phases,
                Image im, Image helper) {
            this.stop = stop;
            this.gates = gates;
            this.threads = threads;
            this.phases = phases;
            this.im = im;
            this.helper = helper;
        }

        public void run() {
            for (int j = 0; j < phases; j++) {
                // wait for all of the threads to stop
                for (int i = 0; i < threads; i++)
                    try { stop.acquire(); } catch (Exception e) {}
                // release all worker threads
                for (int i = 0; i < threads; i++)
                    gates[i].release();
            }
            finished = true;
        }
    }

    private static class SmoothenWorker implements Runnable {
        Semaphore stop;
        Semaphore gate;
        int phases;

        Image im;
        Image helper;
        int row_start;
        int row_end;

        public SmoothenWorker(Semaphore stop, Semaphore gate, int phases,
                Image im, Image helper, int row_start, int row_end) {
            this.stop = stop;
            this.gate = gate;
            this.phases = phases;
            this.im = im;
            this.helper = helper;
            this.row_start = row_start;
            this.row_end = row_end;
        }

        public void run() {
            for (int j = 0; j < phases; j++) {
                // wait for gate
                try { gate.acquire(); } catch (Exception e) {}
                // process a slice of the image
                helper = Image.Smoothen(helper, im, row_start, row_end);
                // swap references for next round
                Image temp = helper;
                helper = im;
                im = temp;
                // indicate work done to barrier thread
                stop.release();
            }
            
        }
    }

    private static class SharpenWorker implements Runnable {
        Semaphore stop;
        Semaphore gate;
        int phases;

        Image im;
        Image helper;
        int row_start;
        int row_end;

        public SharpenWorker(Semaphore stop, Semaphore gate, int phases,
                Image im, Image helper, int row_start, int row_end) {
            this.stop = stop;
            this.gate = gate;
            this.phases = phases;
            this.im = im;
            this.helper = helper;
            this.row_start = row_start;
            this.row_end = row_end;
        }

        public void run() {
            for (int j = 0; j < phases; j++) {
                try { gate.acquire(); } catch (Exception e) {}
                helper = Image.Sharpen(helper, im, row_start, row_end);
                Image temp = helper;
                helper = im;
                im = temp;
                stop.release();
            }

        }
    }
}

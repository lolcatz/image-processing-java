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
public class ImageWorker implements Runnable {
        Semaphore stop;
        Semaphore gate;
        int phases;

        Image im;
        Image helper;
        int row_start;
        int row_end;

        public ImageWorker(Semaphore stop, Semaphore gate, int phases,
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
            // first smoothen
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
            // then sharpen
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

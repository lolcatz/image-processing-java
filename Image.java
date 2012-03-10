/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rio_image_processing;

import java.util.*;
import java.io.*;
/**
 *
 * @author Paul
 */
public class Image {
    byte[] data;
    public int maxval;
    public int width;
    public int height;

    public Image(String filename) throws Exception {

        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);

        if(!br.readLine().equals("P3"))
            throw new Exception("Unsupported format");
        String[] dims = br.readLine().split(" ");
        
        width = Integer.parseInt(dims[0]);
        height = Integer.parseInt(dims[1]);
        maxval = Integer.parseInt(br.readLine());

        data = new byte[height*width*3];

        int read = 0;
        while(read < data.length) {
            String[] values = br.readLine().split(" ");
            for (String b : values)
                data[read++] = (byte)(Integer.parseInt(b) - 128);
        }
        br.close();
        fr.close();
    }

    private int index(int pixel_x, int pixel_y) {
        return (width * pixel_y + pixel_x) * 3;
    }

    public Image(int width, int height, int maxval) {
        this.width = width; this.height = height; this.maxval = maxval;
        data = new byte[height*width*3];
    }

    @Override
    public String toString() {
        String s = "";
        s += "Width: " + width + "\n";
        s += "Height: " + height + "\n";
        s += "Maxval: " + maxval + "\n";
        return s;
    }

    public void Save(String filename) throws Exception {
        File f = new File(filename);
        FileWriter fw = new FileWriter(f);
        fw.append("P3\n");
        fw.append(width + " " +height +"\n");
        fw.append(maxval+"\n");
        for (int i = 0; i < width*height*3; i++) {
            fw.write((data[i]+128)+" ");
            if (i > 0 && i % height == 0)
                fw.append("\n");
        }
        fw.close();
    }

    public static Image Smoothen(Image helper, Image im, int row_start, int row_end) {
        for (int y = row_start; y < row_end; y++) {
            for (int x = 0; x < im.width; x++) {
                int x_start = -1, x_end = 1, y_start = -1, y_end = 1;
                if (x == 0) x_start = 0;
                else if(x == im.width - 1) x_end = 0;
                if (y == 0) y_start = 0;
                else if(y == im.height - 1) y_end = 0;

                int count = 0;
                int[] sums = new int[3];

                for (int y_ = y_start + y; y_ <= y_end + y; y_++)
                    for (int x_ = x_start + x; x_ <= x_end + x; x_++)
                        if (!(x == x_ && y == y_)) {
                            count++;
                            for (int i = 0; i < 3; i++)
                                sums[i] += im.data[im.index(x_,y_)+i];
                        }

                for (int i = 0; i < 3; i++) {
                    sums[i] /= count;
                    helper.data[helper.index(x, y)+i] = (byte)((im.data[im.index(x,y)+i] + sums[i]) / 2);
                }
            }
        }
        return helper;
    }

    public static Image Sharpen(Image helper, Image im, int row_start, int row_end) {
        for (int y = row_start; y < row_end; y++) {
            for (int x = 0; x < im.width; x++) {
                int x_start = -1, x_end = 1, y_start = -1, y_end = 1;
                if (x == 0) x_start = 0;
                else if(x == im.width - 1) x_end = 0;
                if (y == 0) y_start = 0;
                else if(y == im.height - 1) y_end = 0;

                int count = 0;
                int[] sums = new int[3];

                for (int y_ = y_start + y; y_ <= y_end + y; y_++)
                    for (int x_ = x_start + x; x_ <= x_end + x; x_++)
                        if (!(x == x_ && y == y_)) {
                            count++;
                            for (int i = 0; i < 3; i++)
                                sums[i] += im.data[im.index(x_,y_)+i];
                        }

                for (int i = 0; i < 3; i++) {
                    sums[i] /= count;
                    helper.data[helper.index(x, y)+i] = (byte)Math.max(Math.min(2*(int)im.data[im.index(x,y)+i] - sums[i], 127),-128);
                }
            }
        }
        return helper;
    }
}

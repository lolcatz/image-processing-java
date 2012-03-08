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
    //public byte[][][] data;
    byte[][] rdata;
    byte[][] gdata;
    byte[][] bdata;
    public int maxval;
    public int width;
    public int height;

    public Image(String filename) throws Exception {
        Scanner sc = new Scanner(new File(filename));
        if(!sc.nextLine().equals("P3"))
            throw new Exception("Unsupported format");
        width = sc.nextInt();
        height = sc.nextInt();
        maxval = sc.nextInt();

        //data = new byte[height][width][3];
        rdata = new byte[height][width];
        gdata = new byte[height][width];
        bdata = new byte[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                //data[y][x/3][x%3] = (byte)(sc.nextInt() - 128);
                rdata[y][x] = (byte)(sc.nextInt() - 128);
                gdata[y][x] = (byte)(sc.nextInt() - 128);
                bdata[y][x] = (byte)(sc.nextInt() - 128);
            }
    }

    public Image(int width, int height, int maxval) {
        this.width = width; this.height = height; this.maxval = maxval;
        //data = new byte[height][width][3];
        rdata = new byte[height][width];
        gdata = new byte[height][width];
        bdata = new byte[height][width];
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
        for (int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                //for(int i = 0; i < 3; i++)
                //    fw.write((data[y][x][i]+128) + " ");
                fw.write((rdata[y][x]+128) + " ");
                fw.write((gdata[y][x]+128) + " ");
                fw.write((bdata[y][x]+128) + " ");
            }

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

                for (int y_ = y_start+y; y_ <= y_end+y; y_++)
                    for (int x_ = x_start+x; x_ <= x_end+x; x_++)
                        if (!(x == x_ && y == y_)) {
                            count++;
                            //for (int i = 0; i < 3; i++)
                            //    sums[i] += this.data[y_][x_][i];
                            sums[0] += im.rdata[y_][x_];
                            sums[1] += im.gdata[y_][x_];
                            sums[2] += im.bdata[y_][x_];
                        }

                for (int i = 0; i < 3; i++) {
                    sums[i] /= count;
                    //target.data[y][x][i] = (byte)((this.data[y][x][i] + sums[i]) / 2);
                }
                helper.rdata[y][x] = (byte)((im.rdata[y][x] + sums[0]) / 2);
                helper.gdata[y][x] = (byte)((im.gdata[y][x] + sums[1]) / 2);
                helper.bdata[y][x] = (byte)((im.bdata[y][x] + sums[2]) / 2);
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

                for (int y_ = y_start+y; y_ <= y_end+y; y_++)
                    for (int x_ = x_start+x; x_ <= x_end+x; x_++)
                        if (!(x == x_ && y == y_)) {
                            count++;
                            //for (int i = 0; i < 3; i++)
                            //    sums[i] += this.data[y_][x_][i];
                            sums[0] += im.rdata[y_][x_];
                            sums[1] += im.gdata[y_][x_];
                            sums[2] += im.bdata[y_][x_];
                        }

                for (int i = 0; i < 3; i++) {
                    sums[i] /= count;
                    //target.data[y][x][i] = (byte)Math.max(Math.min(2*(int)this.data[y][x][i] - sums[i], 127),-128);
                }
                helper.rdata[y][x] = (byte)Math.max(Math.min(2*(int)im.rdata[y][x] - sums[0], 127),-128);
                helper.gdata[y][x] = (byte)Math.max(Math.min(2*(int)im.gdata[y][x] - sums[1], 127),-128);
                helper.bdata[y][x] = (byte)Math.max(Math.min(2*(int)im.bdata[y][x] - sums[2], 127),-128);
            }
        }
        return helper;
    }
}

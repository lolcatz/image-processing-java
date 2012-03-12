/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
    BufferedWriter bw = new BufferedWriter(fw);

    bw.append("P3\n");
    bw.append(width + " " +height +"\n");
    bw.append(maxval+"\n");
    
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width*3; x++)
        bw.write((data[y*width*3+x]+128)+" ");
      bw.write("\n");
    }
    bw.close();
    fw.close();
  }

  public static Image Smoothen(Image helper, Image im, int row_start, int row_end) {
    for (int y = row_start; y < row_end; y++) {
      for (int x = 0; x < im.width; x++) {
//        int x_start = -1, x_end = 1, y_start = -1, y_end = 1;
//        if (x == 0) x_start = 0;
//        else if(x == im.width - 1) x_end = 0;
//        if (y == 0) y_start = 0;
//        else if(y == im.height - 1) y_end = 0;
//
//        int count = 0;
//        int r_sum = 0;
//        int b_sum = 0;
//        int g_sum = 0;
//
//        for (int y_ = y_start + y; y_ <= y_end + y; y_++)
//          for (int x_ = x_start + x; x_ <= x_end + x; x_++)
//            if (!(x == x_ && y == y_)) {
//              count++;
//              r_sum += im.data[im.index(x_,y_)];
//              g_sum += im.data[im.index(x_,y_)+1];
//              b_sum += im.data[im.index(x_,y_)+2];
//            }

        int count = 0;
        int r_sum = 0;
        int g_sum = 0;
        int b_sum = 0;

        boolean edge_left = false;
        boolean edge_right = false;
        if (x < im.width-1) {
          r_sum += im.data[im.index(x+1,y)];
          g_sum += im.data[im.index(x+1,y)+1];
          b_sum += im.data[im.index(x+1,y)+2];
          count++;
        } else edge_right = true;
        if (x > 0) {
          r_sum += im.data[im.index(x-1,y)];
          g_sum += im.data[im.index(x-1,y)+1];
          b_sum += im.data[im.index(x-1,y)+2];
          count++;
        } else edge_left = true;
        if (y > 0) {
          if (!edge_right) {
            r_sum += im.data[im.index(x+1,y-1)];
            g_sum += im.data[im.index(x+1,y-1)+1];
            b_sum += im.data[im.index(x+1,y-1)+2];
            count++;
          }
          if (!edge_left) {
            r_sum += im.data[im.index(x-1,y-1)];
            g_sum += im.data[im.index(x-1,y-1)+1];
            b_sum += im.data[im.index(x-1,y-1)+2];
            count++;
          }
          r_sum += im.data[im.index(x,y-1)];
          g_sum += im.data[im.index(x,y-1)+1];
          b_sum += im.data[im.index(x,y-1)+2];
          count++;
        }
        if (y < im.height-1) {
          if (!edge_right) {
            r_sum += im.data[im.index(x+1,y+1)];
            g_sum += im.data[im.index(x+1,y+1)+1];
            b_sum += im.data[im.index(x+1,y+1)+2];
            count++;
          }
          if (!edge_left) {
            r_sum += im.data[im.index(x-1,y+1)];
            g_sum += im.data[im.index(x-1,y+1)+1];
            b_sum += im.data[im.index(x-1,y+1)+2];
            count++;
          }
          r_sum += im.data[im.index(x,y+1)];
          g_sum += im.data[im.index(x,y+1)+1];
          b_sum += im.data[im.index(x,y+1)+2];
          count++;
        }


        int index = helper.index(x, y);
        helper.data[index] = (byte)((im.data[index] + r_sum/count) / 2);
        helper.data[index+1] = (byte)((im.data[index+1] + g_sum/count) / 2);
        helper.data[index+2] = (byte)((im.data[index+2] + b_sum/count) / 2);
      }
    }
    return helper;
  }

  public static Image Sharpen(Image helper, Image im, int row_start, int row_end) {
    for (int y = row_start; y < row_end; y++) {
      for (int x = 0; x < im.width; x++) {
//        int x_start = -1, x_end = 1, y_start = -1, y_end = 1;
//        if (x == 0) x_start = 0;
//        else if(x == im.width - 1) x_end = 0;
//        if (y == 0) y_start = 0;
//        else if(y == im.height - 1) y_end = 0;
//
//        int count = 0;
//        int r_sum = 0;
//        int b_sum = 0;
//        int g_sum = 0;
//
//        for (int y_ = y_start + y; y_ <= y_end + y; y_++)
//          for (int x_ = x_start + x; x_ <= x_end + x; x_++)
//            if (!(x == x_ && y == y_)) {
//              count++;
//              r_sum += im.data[im.index(x_,y_)];
//              g_sum += im.data[im.index(x_,y_)+1];
//              b_sum += im.data[im.index(x_,y_)+2];
//            }

        int count = 0;
        int r_sum = 0;
        int g_sum = 0;
        int b_sum = 0;

        boolean edge_left = false;
        boolean edge_right = false;
        if (x < im.width-1) {
          r_sum += im.data[im.index(x+1,y)];
          g_sum += im.data[im.index(x+1,y)+1];
          b_sum += im.data[im.index(x+1,y)+2];
          count++;
        } else edge_right = true;
        if (x > 0) {
          r_sum += im.data[im.index(x-1,y)];
          g_sum += im.data[im.index(x-1,y)+1];
          b_sum += im.data[im.index(x-1,y)+2];
          count++;
        } else edge_left = true;
        if (y > 0) {
          if (!edge_right) {
            r_sum += im.data[im.index(x+1,y-1)];
            g_sum += im.data[im.index(x+1,y-1)+1];
            b_sum += im.data[im.index(x+1,y-1)+2];
            count++;
          }
          if (!edge_left) {
            r_sum += im.data[im.index(x-1,y-1)];
            g_sum += im.data[im.index(x-1,y-1)+1];
            b_sum += im.data[im.index(x-1,y-1)+2];
            count++;
          }
          r_sum += im.data[im.index(x,y-1)];
          g_sum += im.data[im.index(x,y-1)+1];
          b_sum += im.data[im.index(x,y-1)+2];
          count++;
        }
        if (y < im.height-1) {
          if (!edge_right) {
            r_sum += im.data[im.index(x+1,y+1)];
            g_sum += im.data[im.index(x+1,y+1)+1];
            b_sum += im.data[im.index(x+1,y+1)+2];
            count++;
          }
          if (!edge_left) {
            r_sum += im.data[im.index(x-1,y+1)];
            g_sum += im.data[im.index(x-1,y+1)+1];
            b_sum += im.data[im.index(x-1,y+1)+2];
            count++;
          }
          r_sum += im.data[im.index(x,y+1)];
          g_sum += im.data[im.index(x,y+1)+1];
          b_sum += im.data[im.index(x,y+1)+2];
          count++;
        }

        int index = helper.index(x, y);
        helper.data[index] = byteClamp(2*(int)im.data[index] - r_sum/count);
        helper.data[index+1] = byteClamp(2*(int)im.data[index+1] - g_sum/count);
        helper.data[index+2] = byteClamp(2*(int)im.data[index+2] - b_sum/count);
      }
    }
    return helper;
  }

  static byte byteClamp(int i) {
    return i < -128 ? -128 : i > 127 ? 127 : (byte)i;
  }
}

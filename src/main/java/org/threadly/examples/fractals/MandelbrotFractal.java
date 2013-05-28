package org.threadly.examples.fractals;

public class MandelbrotFractal {
  public static int calculatePixel(long x, long y, long width, long height, int offset) {
    int result = offset;
    double x0 = -2 + (x / (width * 0.37));
    double y0 = -1.2 + (y / (height * 0.4));
    
    double xx = 0;
    double yy = 0;
    
    int iteration = 0;
    int max_iterations = 1000;
    
    while (xx * xx + yy * yy <= (2 * 2) && 
             iteration < max_iterations) {
      double xtemp = xx * xx - yy * yy + x0;
      yy = 2 * xx * yy + y0;
      
      xx = xtemp;
      
      iteration++;
    }
    
    if (iteration == max_iterations && 
        xx * xx + yy * yy <= (2 * 2)) {
      result = 0;
    } else {
      result += iteration;
    }
    
    return result + offset;
  }
}
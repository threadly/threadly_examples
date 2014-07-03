package org.threadly.examples.fractals;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.util.ExceptionUtils;

public class ThreadlyFractal {
  protected static final int windowWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
  protected static final int windowHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
  private static final PriorityScheduler scheduler;
  
  static {
    int processors = Runtime.getRuntime().availableProcessors();
    scheduler = new PriorityScheduler(processors * 2, processors * 2, 
                                      1000, TaskPriority.High, 500);
  }
  
  private static Map<Long, ListenableFuture<int[]>> futureMap = new HashMap<Long, ListenableFuture<int[]>>();
  private static Image image;
  private static long fractalWidth = windowWidth;
  private static long fractalHeight = windowHeight;
  private static long xOffset = 0;
  private static long yOffset = 0;
  
  public static void main(String[] args) throws Exception {
    displayFractal();
  }
  
  private static void displayFractal() {
    updateImage();

    Frame frame = new Frame("Fractal");
    frame.add(new FractalCanvas(frame));
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    frame.setSize(windowWidth, windowHeight);
    
    frame.setVisible(true);
  }
  
  protected static void submitCallables(final int width, final int height) {
    for (long y = yOffset; y < height + yOffset; y++) {
      final long f_y = y;
      futureMap.put(y, scheduler.submit(new Callable<int[]>() {
        @Override
        public int[] call() {
          int[] result = new int[width];
          int index = 0;
          for (long x = xOffset; x < width + xOffset; x++) {
            result[index] = MandelbrotFractal.calculatePixel(x, f_y, fractalWidth, fractalHeight, 
                                                             0xFF000000);
            // create a little background
            double a = Math.sqrt(x * (width / (double)fractalWidth));
            double b = Math.sqrt(f_y * (height / (double)fractalHeight));
            result[index++] += (int) Math.sqrt(a + b);
          }
          int percentDone = (int)((((double)f_y - yOffset) / height) * 100);
          // little extra check to avoid reporting multiple times due to int precision
          if (percentDone != (int)((((double)f_y + 1 - yOffset) / height) * 100)) {
            if (percentDone % 10 == 0) {
              System.out.println(percentDone + "% done");
            }
          }
          
          return result;
        }
      }));
    }
  }
  
  protected static int[] composeResults(int width, int height) {
    int[] imageData = new int[width * height];
    for (long y = yOffset; y < height + yOffset; y++) {
      int indexStart = (int)(width * (y - yOffset));
      int[] result;
      try {
        result = futureMap.get(y).get();
      } catch (ExecutionException e) {
        throw ExceptionUtils.makeRuntime(e);
      } catch (InterruptedException e) {
        throw ExceptionUtils.makeRuntime(e);
      }
      System.arraycopy(result, 0, imageData, indexStart, result.length);
    }
    
    return imageData;
  }
  
  protected static int[] generateImageData(int width, int height) {
    submitCallables(width, height);
    
    return composeResults(width, height);
  }
  
  private static void updateImage() {
    System.out.println("Generating image...Size: " + fractalWidth + "x" + fractalHeight + 
                         ", Position: " + yOffset + "x" + xOffset);
    int[] imageData = generateImageData(windowWidth, windowHeight);
    System.out.println("Done generating fractal");
    
    image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(windowWidth, windowHeight, 
                                                                          imageData, 0, windowWidth));
  }

  private static class FractalCanvas extends Canvas 
                                     implements MouseListener {
    private static final long serialVersionUID = -2909907873906146984L;

    private final Frame frame;
    private Point pressedPoint;
    
    private FractalCanvas(Frame frame) {
      this.frame = frame;
      pressedPoint = null;
      
      addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (e.getButton() == 3) { // reset image
        frame.setVisible(false);
        
        fractalWidth = windowWidth;
        fractalHeight = windowHeight;
        xOffset = 0;
        yOffset = 0;

        displayFractal();
      }
    }

    @Override
    public void mousePressed(MouseEvent e) {
      if (e.getButton() == 1) {
        pressedPoint = e.getPoint();
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.getButton() == 1) {
        frame.setVisible(false);
        
        final int startPointX = pressedPoint.x;
        final int startPointY = pressedPoint.y;
        final int endPointX = e.getPoint().x;
        final int endPointY = e.getPoint().y;
        
        scheduler.execute(new Runnable() {
          @Override
          public void run() {
            // calculate selected distances based off total image size
            long xDistance = Math.abs(endPointX - startPointX) * (fractalWidth / windowWidth);
            long yDistance = Math.abs(endPointY - startPointY) * (fractalHeight / windowHeight);
            if (xDistance == 0 || yDistance == 0) {
              System.out.println("Section too small, ignoring zoom");
              displayFractal();
              return;
            }
            
            // figure out how to scale all our values
            double aspect = ((double)windowWidth) / windowHeight;
            double selectedAspect = ((double) xDistance) / yDistance;
            double scaleFactor;
            if (selectedAspect > aspect) {  // depend on x
              scaleFactor = ((double)fractalWidth) / xDistance;
            } else {  // depend on y
              scaleFactor = ((double)fractalHeight) / yDistance;
            }
            
            // update values based off scale factory
            fractalWidth = (long)(fractalWidth * scaleFactor);
            fractalHeight = (long)(fractalHeight * scaleFactor);
            xOffset = (long)((xOffset + (startPointX < endPointX ? startPointX : endPointX)) * scaleFactor);
            yOffset = (long)((yOffset + (startPointY < endPointY ? startPointY : endPointY)) * scaleFactor);
            
            displayFractal();
          }
        });
      }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      //ignored
    }

    @Override
    public void mouseExited(MouseEvent e) {
      // ignored
    }
    
    @Override
    public void paint(Graphics g) {
      g.drawImage(image, 0, 0, null);
    }
  }
}
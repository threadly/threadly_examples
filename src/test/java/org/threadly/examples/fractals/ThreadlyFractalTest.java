package org.threadly.examples.fractals;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.threadly.concurrent.CallableDistributor;
import org.threadly.concurrent.PriorityScheduledExecutor;
import org.threadly.concurrent.TaskExecutorDistributor;
import org.threadly.concurrent.TaskPriority;

public class ThreadlyFractalTest {
  @Test @Ignore
  public void generateImageDataTest() {
    final int width = 20;
    final int height = 15;
    
    PriorityScheduledExecutor scheduler = new PriorityScheduledExecutor(100, 1000, 1000, 
                                                                        TaskPriority.High, 500);
    TaskExecutorDistributor executorDistributor = new TaskExecutorDistributor(scheduler);
    final CallableDistributor<Long, int[]> cd = new CallableDistributor<Long, int[]>(executorDistributor);
    
    ThreadlyFractal.submitCallables(cd, width, height);
    int[] result = ThreadlyFractal.generateImageData(cd, width, height);  // collect all the results previously generated
    
    assertEquals(result.length, 
                 ThreadlyFractal.windowWidth * ThreadlyFractal.windowHeight);
    for (int i = 0; i < result.length; i++) {
      assertNotNull(result[i]);
      assertTrue(result[i] > 0);
    }
  }
}

package org.threadly.examples.fractals;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.threadly.concurrent.CallableDistributor;
import org.threadly.concurrent.PriorityScheduledExecutor;
import org.threadly.concurrent.TaskExecutorDistributor;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.lock.StripedLock;
import org.threadly.test.concurrent.TestablePriorityScheduler;

/**
 * Simple test that shows a very VERY basic way of using the 
 * {@link TestablePriorityScheduler}.  The fractal program does 
 * not really need to be tested this way, but I wanted to further 
 * demonstrate the use of Threadly.
 * 
 * @author jent - Mike Jensen
 */
public class ThreadlyFractalTest {
  @Test @Ignore
  public void generateImageDataTest() {
    final int width = 20;
    final int height = 15;
    
    PriorityScheduledExecutor scheduler = new PriorityScheduledExecutor(100, 1000, 1000, 
                                                                        TaskPriority.High, 500);
    final TestablePriorityScheduler testScheduler = new TestablePriorityScheduler(scheduler);
    TaskExecutorDistributor executorDistributor = new TaskExecutorDistributor(testScheduler, 
                                                                              new StripedLock(100, testScheduler));
    final CallableDistributor<Long, int[]> cd = new CallableDistributor<Long, int[]>(executorDistributor);
    
    ThreadlyFractal.submitCallables(cd, width, height);
    testScheduler.tick();  // run all callables at this point
    int[] result = ThreadlyFractal.generateImageData(cd, width, height);  // tquickly collect all the results previously generated
    
    assertEquals(result.length, 
                 ThreadlyFractal.windowWidth * ThreadlyFractal.windowHeight);
    for (int i = 0; i < result.length; i++) {
      assertNotNull(result[i]);
      assertTrue(result[i] > 0);
    }
  }
}

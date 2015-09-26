package org.threadly.examples.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.TaskPriority;
import org.threadly.concurrent.future.FutureUtils;
import org.threadly.concurrent.future.ListenableFuture;

/**
 * Provides a basic example of the PriorityScheduler feature.
 * 
 * @author sreid8 - Sean Reid
 *
 */
public class PrioritySchedulerExample {
  
  /**
   * Main entry point of the example.
   * @param args -> program args. run w/o args for help
   */
  public static void main(String[] args) {
    if (!handleArgs(args)) {
      System.exit(1);
    }
    
    final int numThreads = Integer.parseInt(args[0]);
    
    //create the PriorityScheduler. This is where we will make a method to handle the case of different
    //priorities!!!
    final PriorityScheduler executor = new PriorityScheduler(numThreads - 1, true);
    //initialize all threads. threads will be idle until a task is provided
    executor.prestartAllThreads();
    
    //create a list of ListenableFutures (java.util.concurrent.Future + the ability to listen for
    //completion of task) with the number of threads in the thread pool.
    List<ListenableFuture<?>> futures = new ArrayList<ListenableFuture<?>>(numThreads - 1);
    
    //add numThreads - 1 tasks to the PriorityScheduler.
   for (int i = 0; i < numThreads; i++) {
     //random between 0-2 to determine priority.
     TaskPriority priority = getRandomPriority();
     System.out.println("Submitting thread " + (i + 1) + " with priority " + priority.toString());
     futures.add(executor.submit(new BasicRunnable(i), priority));
   }
   
   //run the numThreads-th task on the main thread so that it is blocking.
   BasicRunnable runner = new BasicRunnable(numThreads);
   runner.run();
   
   //check to make sure that all the threads have completed
   try {
     FutureUtils.blockTillAllCompleteOrFirstError(futures);
   } catch (Exception e) {
     //normally would be ExecutionException and InterrupedException
     e.printStackTrace();
   }

    
    
   }
  
  /**
   * handles the args on startup
   */
  private static Boolean handleArgs(final String[] args) {
    //handles the args, decides whether to call help and validates data in args
    try {
      Integer.parseInt(args[0]);
      //Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      printHelp();
    }
    return true;
  }
  
  /**
   * Prints the information
   */
  public static void printHelp() {
    System.out.println("format: java -cp org.threadly.examples.features.PrioritySchedulerExample numberOfThreads priorityValue");
    System.out.println("\nPriority Values:");
  }
  
  /**
   * gets a pseudorandom TaskPriority value
   */
  private static TaskPriority getRandomPriority() {
    Random rand = new Random();
    int randNum = rand.nextInt((2) + 1);
    if (randNum == 0) {
      return TaskPriority.Starvable;
    } else if (randNum == 1) {
      return TaskPriority.Low;
    } else {
      return TaskPriority.High;
    }
  }
  
  
  /**
   * This is a generic Runnable that the PriorityScheduler can run.
   * 
   */
  private static class BasicRunnable implements Runnable {
    
    /**
     * the id of the thread which will be reported to stdout
     */
    private final int runNumber;
    
    /**
     * the average execution time of the iterations
     */
    private double[] times;
    
    /**
     * the start time of the runnable
     */
    private final double startTime;
    
    /**
     * Parameterized ctor. note: parameters are not required for a runnable to be executed by the PriorityScheduler.
     * @param pRunNumber -> the id number of the thread which thread will report to stdout
     */
    public BasicRunnable(final int pRunNumber) {
      this.runNumber = pRunNumber;
      this.times = new double[10];
      this.startTime = System.nanoTime();
    }
    
    @Override
    public void run() {
      for (int i = 0; i < 10; i++) {
        
        if (i == 0) {
          times[i] = 0;
        } else {
          times[i] = System.nanoTime() - times[i-1];
        }
        System.out.print("Thread "
                           + runNumber
                           + " is on iteration "
                           + (i+1)
                           + ". ");
      }
      System.out.println("Thread " + runNumber + " took " + (startTime - System.nanoTime()) + " to execute." );
    }
    
    
  }
  
}

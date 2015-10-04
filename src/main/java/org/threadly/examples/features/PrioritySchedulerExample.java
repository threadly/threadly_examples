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
  
  private static Boolean threadDetail = false;
  
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
    
    //create a list of ListenableFutures (java.util.concurrent.Future + the ability to listen for
    //completion of task) with the number of threads in the thread pool.
    List<ListenableFuture<?>> futures = new ArrayList<ListenableFuture<?>>(numThreads - 1);
    
    //add numThreads - 1 tasks to the PriorityScheduler.
   for (int i = 0; i <= numThreads; i++) {
     //random between 0-2 to determine priority.
     TaskPriority priority = getRandomPriority();
     System.out.println("Submitting thread " + (i) + " with priority " + priority.toString());
     futures.add(executor.submit(new BasicRunnable(i, threadDetail), priority));
   }
   /*
    * Initialize the threads. This can be done BEFORE or AFTER submitting Runnables to the PriorityScheduler
    * If done BEFORE:
    *   Threads are created and are sitting idle. As soon as a task is submitted, an available thread will begin to work the task.
    * If done AFTER:
    *   Threads begin working on tasks immediately as they already have tasks.
    */
   executor.prestartAllThreads();
   
   //block until all threads are complete
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
    } catch (NumberFormatException e) {
      printHelp();
      return false;
    } catch (IndexOutOfBoundsException e) {
      printHelp();
      return false;
    }
    try {
      if (args[1].equalsIgnoreCase("-v")) {
        PrioritySchedulerExample.threadDetail = true;
      }
    } catch (IndexOutOfBoundsException e) {
      PrioritySchedulerExample.threadDetail = false;
    }
    return true;
  }
  
  /**
   * Prints the information
   */
  public static void printHelp() {
    System.out.println("Priority Scheduler Example executes a number of threads with a known, but randomly assigned. Each thread counts to the Max Integer Value before exiting.");
    System.out.println("args: numThreads -> the integer numner of threads to be executed");
    System.out.println("optional args: \"-v\" -> increases the amount of data reported by each thread and reduced the threads to counting to 100");
    System.out.println("ex: PrioritySchedulerExample 1000 -v");
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
     * the start time of the runnable
     */
    private final double startTime;
    
    /**
     * whether or not to display detail
     */
    private final Boolean detail;
    
    /**
     * Parameterized ctor. note: parameters are not required for a runnable to be executed by the PriorityScheduler.
     * @param pRunNumber -> the id number of the thread which thread will report to stdout
     * @param pDetail -> whether or not to show verbose output (suppress for large number of threads for readability)
     */
    public BasicRunnable(final int pRunNumber, final Boolean pDetail) {
      this.runNumber = pRunNumber;
      this.startTime = System.nanoTime();
      this.detail = pDetail;
    }
    
    @Override
    public void run() {
      for (int i = 0; i < (detail ? 100 : Integer.MAX_VALUE); i++) {
        if (detail) {
          System.out.println("Thread " + runNumber + " is on iteration " + (i) + ". ");
        }
      }
      System.out.println("Thread " + runNumber + " took " + (System.nanoTime() - startTime) + "  nanoseconds to execute." );
    }
    
    
  }
  
}

package org.threadly.examples.features;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.threadly.concurrent.future.FutureUtils;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.TaskPriority;


/**
 * Provides a basic example of the PriorityScheduler feature.
 * 
 * @author sreid8 - Sean Reid
 */
public final class PrioritySchedulerExample {
  private Runnable recurring = null;
  
  /** reference to the futures created */ 
  private List<ListenableFuture<?>> statFutures;
  
  /**
   * Provided a PriorityScheduler that contains some basic ideas.
   * 
   * @param executor a PriorityScheduler instance
   * @return the PriorityScheduler with tasks added
   */
  public PriorityScheduler addTasksToPriorityScheduler(PriorityScheduler executor) {
    final int numThreads = Runtime.getRuntime().availableProcessors() * 2;
    List<ListenableFuture<?>> futures = new ArrayList<ListenableFuture<?>>(numThreads);
    recurring = new Runnable() {
      @Override
      public void run() {
        /*
         * This is the task that will be run on a schedule.
         * Example use case: regular cleanup/system maintenance task
         */
      }
    };
    //add a recurring task for execution at a fixed time interval
    executor.scheduleWithFixedDelay(recurring, 0, 100000);
    
    futures.add(executor.submit(new Runnable() {
      @Override
      public void run() {
        /*
         * This is a low priority task.
         * Low priority is run before Starvable Priority tasks 
         * and after High Priority tasks.
         * Example: stats gathering for long running task
         */
      }
    }, TaskPriority.Low));
    
    futures.add(executor.submit(new Runnable() {
      @Override
      public void run() {
        /*
         * This is the High priority task that will be run.
         * High priority tasks are the highest priority in the thread pool.
         * Example use case: Servicing requests for the application
         * or doing processing that requires results as quickly as possible
         */
      } 
     }, TaskPriority.High));
    
    //adding new high priority tasks to fill the pool
    for (int i = 0; i < numThreads - 3; i++) {
      futures.add(executor.submit(new HighPriorityTask(), TaskPriority.High));
    }
    //assigning to the references for use in other methods
    statFutures = futures;
    
    return executor;
  }
  
  /**
   * Gets the list of Listenable Futures to help with blocking. relies on getPriorityScheduler to 
   * be executed before this will return non-null.
   * 
   * @return a list of ListenableFutures, null if this has not been set
   */
  public List<ListenableFuture<?>> getFutures() {
    return statFutures;
  }
  
  /**
   * 
   * @param executor a PriorityScheduler
   * @return the same PriorityScheduler, but with the tasks started
   */
  public static PriorityScheduler startTasks(final PriorityScheduler executor) {
    /*
     * the prestartAllThreads() method can be called before or after
     * tasks have been submitted
     */
    executor.prestartAllThreads();
    return executor;
  }
  
  /**
   * 
   * @param executor a PriorityScheduler instance
   * @param futures the list of ListenableFutures
   * @return the same PriorityScheduler, but after tasks have been executed
   */
  public static PriorityScheduler startTasksBlocking(final PriorityScheduler executor, final List<ListenableFuture<?>> futures) {
    executor.prestartAllThreads();
    try {
      FutureUtils.blockTillAllCompleteOrFirstError(futures);
    } catch (InterruptedException e) {
      //should handle the InterruptedExeception
      e.printStackTrace();
    } catch (ExecutionException e) {
      //should handle the ExecutionException
      e.printStackTrace();
    }
    return executor;
  }
  
  /**
   * prevents any future tasks from being submitted to the PriorityScheduler.
   * 
   * @param executor PriorityScheduler to operate on
   * @param recurringStop whether or not to stop recurring tasks as well
   */
  public void disposePrioritySchedulerTasks(final PriorityScheduler executor, boolean recurringStop) {
    if (recurringStop) {
      if (this.recurring != null) {
        executor.remove(this.recurring);
      }
      executor.shutdown();
    } else {
      executor.shutdown();
    }
  }
  
  /**
   * A basic runnable that can be reused
   */
  private class HighPriorityTask implements Runnable {
    @Override
    public void run() {
      /*
       * A high priority task would be here. Something similar to the other
       * High Priority task outlined above.
       */
    }
  }
}

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
 *
 */
public final class PrioritySchedulerExample {
  
  /** static reference to the futures created */ 
  private static List<ListenableFuture<?>> statFutures;
  
  /**
   * Provided a PriorityScheduler that contains some basic ideas
   * @return -> the PriorityScheduler with tasks added
   */
  public static PriorityScheduler getPriorityScheduler() {
    final int numThreads = Runtime.getRuntime().availableProcessors() * 2;
    final PriorityScheduler executor = new PriorityScheduler(numThreads);
    List<ListenableFuture<?>> futures = new ArrayList<ListenableFuture<?>>(numThreads);
    
    futures.add(executor.submit(new Runnable() {
      
      @Override
      public void run() {
        /*
         * This is the Starvable priority task that will be run.
         * Starvable tasks run only when no other task needs to run
         * It can be used to keep track of configuration:
         * Example use case: closing sockets that no longer have hosts
         * available on the other side.
         */
        
      }
    },
    /*The priority of the task we are submitting*/TaskPriority.Starvable));
    
    futures.add(executor.submit(new Runnable() {
      
      @Override
      public void run() {
        /*
         * This is the Low priority task that will be run.
         * Low priority tasks are run before Starvable tasks,
         * but exactly when it will be run is enforced by the 
         * PrioritySchedulerService.
         * Example use case: cache cleaning or other cleanup work
         */
        
      }
    }, TaskPriority.Low));
    
    futures.add(executor.submit(new Runnable() {

      @Override
      public void run() {
        /*
         * This is the High priority task that will be run.
         * High priority tasks are the highest priority in the thread pool.
         * Example use case: Stats reporting about number of threads active in the pool
         * and how many are queued.
         */
        
      } 
     }, TaskPriority.High));
    
    //adding new high priority tasks to fill the pool
    for (int i = 0; i < numThreads - 3; i++) {
      futures.add(executor.submit(new HighPriorityTask(), TaskPriority.High));
    }
    //assigning to the static references for use in other methods
    statFutures = futures;
    
    
    return executor;
  }
  
  /**
   * gets the list of Listenable Futures to help with blocking. relies on getPriorityScheduler to be executed before this will return non-null
   * @return -> a list of ListenableFutures, null if this has not been set
   */
  public static List<ListenableFuture<?>> getFutures() {
    return statFutures;
  }
  
  /**
   * 
   * @param executor -> a PriorityScheduler
   * @return -> the same PriorityScheduler, but with the tasks started
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
   * @param executor -> a PriorityScheduler
   * @param futures -> the list of ListenableFutures
   * @return -> the same PriorityScheduler, but after tasks have been executed
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
   * A basic runnable that can be reused
   */
  private static class HighPriorityTask implements Runnable {

    @Override
    public void run() {
      /*
       * A high priority task would be here. Something similar to the other
       * High Priority task outlined above.
       */
      
    }
    
  }
}

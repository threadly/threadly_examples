package org.threadly.examples.prime;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.threadly.concurrent.PriorityScheduler;
import org.threadly.concurrent.future.ListenableFuture;
import org.threadly.util.ExceptionUtils;

/**
 * <p>Tester which takes a (ideally) very large number as an argument, and in parallel determines 
 * if it is prime or not.</p>
 * 
 * @author jent - Mike Jensen
 */
public class NextPrime {
  @SuppressWarnings("javadoc")
  public static void main(final String args[]) throws InterruptedException {
    if (args.length == 0) {
      System.err.println("No number to test provided");
      System.err.println("Usage: java -cp threadly_examples.jar " + 
                           NextPrime.class.getName() + " [number to start search from]...");
      System.exit(1);
    }
    
    final int processingThreads = Runtime.getRuntime().availableProcessors() * 2;
    int threadPoolSize = processingThreads + args.length - 1;
    final PriorityScheduler executor = new PriorityScheduler(threadPoolSize, true);
    executor.prestartAllThreads();
    
    Deque<ListenableFuture<PrimeResult>> futures = new ArrayDeque<ListenableFuture<PrimeResult>>(processingThreads * 2);
    
    int value = Integer.parseInt(args[0]) + 1;
    while (true) {
      final int f_i = value++;
      futures.add(executor.submit(new Callable<PrimeResult>() {
        @Override
        public PrimeResult call() throws InterruptedException {
          return testNumber(executor, processingThreads, f_i);
        }
      }));
      
      boolean firstRun = true;
      while (firstRun || futures.size() > processingThreads * 2) {
        firstRun = false;
        Iterator<ListenableFuture<PrimeResult>> it = futures.iterator();
        while (it.hasNext()) {
          ListenableFuture<PrimeResult> f = it.next();
          if (f.isDone()) {
            try {
              PrimeResult result = f.get();
              if (result.isPrime) {
                System.out.println("The next prime number is: " + result.testNumber);
                System.exit(0);
              } else {
                it.remove();
              }
            } catch (ExecutionException e) {
              throw ExceptionUtils.makeRuntime(e.getCause());
            }
          } else {
            break;
          }
        }
        
        Thread.sleep(10);
      }
    }
  }
  
  private static PrimeResult testNumber(PriorityScheduler executor, 
                                    int threadCount, int number) throws InterruptedException {
    // TODO - improve this with an AKS implementation
    PrimeProcessor primeProcessor = new DumbTester(new BigInteger(Integer.toString(number)));
    return new PrimeResult(number, primeProcessor.isPrime(executor, threadCount));
  }
  
  private static class PrimeResult {
    public final int testNumber;
    public final boolean isPrime;
    
    public PrimeResult(int testNumber, boolean isPrime) {
      this.testNumber = testNumber;
      this.isPrime = isPrime;
    }
  }
}

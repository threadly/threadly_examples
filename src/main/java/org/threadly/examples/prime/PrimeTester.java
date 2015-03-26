package org.threadly.examples.prime;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.threadly.concurrent.PriorityScheduler;
import org.threadly.util.ExceptionUtils;

/**
 * <p>Tester which takes a (ideally) very large number as an argument, and in parallel determines 
 * if it is prime or not.</p>
 * 
 * @author jent - Mike Jensen
 */
public class PrimeTester {
  @SuppressWarnings("javadoc")
  public static void main(final String args[]) throws InterruptedException {
    if (args.length == 0) {
      System.err.println("No number to test provided");
      System.err.println("Usage: java -cp Threadly_Examples.jar " + 
                           PrimeTester.class.getName() + " [number to test]...");
      System.exit(1);
    }
    
    final int processingThreads = Runtime.getRuntime().availableProcessors() * 2;
    int threadPoolSize = processingThreads + args.length - 1;
    final PriorityScheduler executor = new PriorityScheduler(threadPoolSize, true);
    executor.prestartAllThreads();
    
    List<Future<?>> futures = new ArrayList<Future<?>>(args.length);
    
    for (int i = 0; i < args.length - 1; i++) {
      final int f_i = i;
      futures.add(executor.submit(new Runnable() {
        @Override
        public void run() {
          try {
            testNumber(executor, processingThreads, args[f_i]);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }));
    }
    
    // last one we always wait for on the main thread
    testNumber(executor, processingThreads, args[args.length - 1]);
    
    // verify all others finished
    Iterator<Future<?>> it = futures.iterator();
    while (it.hasNext()) {
      try {
        it.next().get();
      } catch (ExecutionException e) {
        throw ExceptionUtils.makeRuntime(e);
      }
    }
  }
  
  private static void testNumber(PriorityScheduler executor, 
                                 int threadCount, String number) throws InterruptedException {
    // TODO - improve this with an AKS implementation
    PrimeProcessor primeProcessor = new DumbTester(new BigInteger(number));
    
    boolean isPrime = primeProcessor.isPrime(executor, threadCount);
    if (isPrime) {
      System.out.println(number + " is prime!");
    } else {
      System.out.println(number + " is divisible by: " + primeProcessor.getFactor());
    }
  }
}

package org.threadly.examples.prime;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import org.threadly.util.ExceptionUtils;

/**
 * <p>A very simple and stupid implementation to test prime numbers.  It divides the work in 
 * parallel, but just does a modulus on the number compared to every other odd number that is less 
 * than the provided number.</p>
 * 
 * @author jent - Mike Jensen
 */
public class DumbTester implements PrimeProcessor {
  private static final BigInteger TWO = BigInteger.ONE.add(BigInteger.ONE);
  
  private final BigInteger n;
  private BigInteger factor;
  
  /**
   * Constructs a tester with a very basic test algorithm.
   * 
   * @param n Number to test against
   */
  public DumbTester(BigInteger n) {
    this.n = n;
    factor = null;
  }
  
  @Override
  public BigInteger getFactor() {
    return factor;
  }
  
  @Override
  public boolean isPrime(Executor executor, int parallelLevel) throws InterruptedException {
    if (factor != null) {
      return false;
    }
    
    // quick check for even numbers
    if (n.mod(TWO).equals(BigInteger.ZERO)) {
      factor = TWO;
      return false;
    }
    
    ExecutorCompletionService<BigInteger> ecs = new ExecutorCompletionService<BigInteger>(executor);
    List<Future<BigInteger>> futures = new ArrayList<Future<BigInteger>>(parallelLevel);
    
    // low numbers are not worth executing out
    BigInteger valuesPerThread = n.divide(BigInteger.valueOf(parallelLevel));
    if (valuesPerThread.intValue() < 10) {
      factor = new PrimeWorker(n, BigInteger.valueOf(3), n).call();
      
      return factor == null;
    }
    
    for (int i = 0; i < parallelLevel; i++) {
      futures.add(ecs.submit(new PrimeWorker(n, valuesPerThread.multiply(BigInteger.valueOf(i)), 
                                             valuesPerThread.multiply(BigInteger.valueOf(i + 1)))));
    }
    
    for (int i = 0; i < parallelLevel; i++) {
      Future<BigInteger> future = ecs.take();
      try {
        if (future.get() != null) {
          factor = future.get();
          Iterator<Future<BigInteger>> it = futures.iterator();
          while (it.hasNext()) {
            it.next().cancel(true);
          }
          return false;
        }
      } catch (ExecutionException e) {
        throw ExceptionUtils.makeRuntime(e);
      }
    }
    
    return true;
  }
  
  private static class PrimeWorker implements Callable<BigInteger> {
    private final BigInteger testVal;
    private final BigInteger startVal;
    private final BigInteger endVal;
    
    private PrimeWorker(BigInteger testVal, 
                        BigInteger startVal, 
                        BigInteger endVal) {
      this.testVal = testVal;
      if (startVal.mod(TWO).equals(BigInteger.ZERO)) {
        startVal = startVal.add(BigInteger.ONE);
      }
      if (startVal.equals(BigInteger.ONE)) {
        startVal = startVal.add(TWO);
      }
      this.startVal = startVal;
      if (endVal.equals(testVal)) {
        endVal = endVal.subtract(BigInteger.ONE);
      }
      this.endVal = endVal;
    }
    
    @Override
    public BigInteger call() throws InterruptedException {
      for (BigInteger currentVal = startVal; 
           currentVal.compareTo(endVal) <= 0; 
           currentVal = currentVal.add(TWO)) {
        if (testVal.mod(currentVal).equals(BigInteger.ZERO)) {
          return currentVal;
        } else if (Thread.interrupted()) {
          throw new InterruptedException();
        }
      }
      
      return null;
    }
  }
}

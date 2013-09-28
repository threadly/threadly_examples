package org.threadly.examples.prime;

import java.math.BigInteger;
import java.util.concurrent.Executor;

public interface PrimeProcessor {
  /**
   * Does the calculation to see if a number is prime or not.  This returns a 
   * boolean with the fine result from the in parallel calculation.
   * 
   * @param executor executor to call work on to
   * @param parallelLevel amount of executions to submit to the executor
   * @return true if the number is prime
   * @throws InterruptedException thrown if the thread is interupted while waiting for a result
   */
  public boolean isPrime(Executor executor, int parallelLevel) throws InterruptedException;
  
  /**
   * Call to get the factor of a number after isPrime has been called.  If isPrime has not 
   * been called yet, this will return null.  If the number is prime, this will always be null.
   * 
   * @return factor of the number provided to the processor
   */
  public BigInteger getFactor();
}

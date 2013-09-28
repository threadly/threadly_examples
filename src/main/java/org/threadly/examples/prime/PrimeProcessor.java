package org.threadly.examples.prime;

import java.math.BigInteger;
import java.util.concurrent.Executor;

public interface PrimeProcessor {
  public boolean isPrime(Executor executor, int parallelLevel) throws InterruptedException;
  
  public BigInteger getFactor();
}

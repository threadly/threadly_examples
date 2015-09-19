package org.threadly.examples.features;

/**
 * Provides a basic example of the PriorityScheduler feature.
 * 
 * @author sreid8
 *
 */
public class PrioritySchedulerExample {
  
  /**
   * Main entry point of the example.
   * @param args -> program args. run w/o args for help
   */
  public static void main(String[] args) {
    if (args.length >= 1) {
      printHelp();
    }
   }
  
  /**
   * handles the args on startup
   */
  private static void handleArgs(final String[] args) {
    //handles the args, decides whether to call help and validates data in args
  }
  
  /**
   * Prints the information
   */
  public static void printHelp() {
    //the help information
  }
  
  
  /**
   * This is a generic Runnable that the PriorityScheduler can run.
   * 
   */
  private class BasicRunnable implements Runnable {
    
    /**
     * the id of the thread which will be reported to stdout
     */
    private final int runNumber;

    /**
     * the time that the thread ended last time it executed
     */
    private long prevEndTime;
    
    /**
     * Parameterized ctor 
     * @param pRunNumber -> the id number of the thread which thread will report to stdout
     */
    public BasicRunnable(final int pRunNumber) {
      this.runNumber = pRunNumber;
    }
    
    @Override
    public void run() {
      for (int i = 0; i < 10; i++) {
        //waste some time
      }
      //print time since last exe and thread id
    }
    
  }
  
}

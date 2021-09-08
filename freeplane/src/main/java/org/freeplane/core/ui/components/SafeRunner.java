package org.freeplane.core.ui.components;

public class SafeRunner {
    private static final int WAITING_TIME_AFTER_FAILURE_MILLIS = 50;

    public static void run(int remainingAttempts, Runnable runnable) {
        for(;;) {
            try {
                runnable.run();
                return;
            }
            catch (RuntimeException e) {
                remainingAttempts--;
                if(remainingAttempts <= 0) {
                    String errorMessage = e.getMessage();
                    if(errorMessage != null)
                        UITools.errorMessage(errorMessage);
                    return;
                }
            }
            try {
                Thread.sleep(WAITING_TIME_AFTER_FAILURE_MILLIS);
            } catch (InterruptedException e) {/**/}
        }
    }
}
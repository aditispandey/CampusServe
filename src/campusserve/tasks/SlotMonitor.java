package campusserve.tasks;

import campusserve.util.ActivityLog;

/**
 * THREAD CLASS (extends Thread). Wakes up every few seconds and runs 'onTick'
 * (the volunteer dashboard uses it to refresh the live open-slots label).
 * Life cycle:  NEW -> RUNNABLE -> TIMED_WAITING (sleeping) -> TERMINATED
 */
public class SlotMonitor extends Thread {
    private final long intervalMs;
    private final Runnable onTick;
    private volatile boolean running = true;

    public SlotMonitor(long intervalMs, Runnable onTick) {
        super("SlotMonitor");
        this.intervalMs = intervalMs;
        this.onTick = onTick;
        setDaemon(true);                    // does not keep the program alive
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(intervalMs);   // state: TIMED_WAITING
            } catch (InterruptedException e) {
                break;                      // stopMonitor() interrupted us
            }
            if (running) onTick.run();
        }
    }

    public void stopMonitor() {
        running = false;
        interrupt();
        try {
            join(1000);                     // wait until the thread is really finished
        } catch (InterruptedException e) {
            // ignore
        }
        ActivityLog.log("SlotMonitor state after stop: " + getState());    // TERMINATED
    }
}

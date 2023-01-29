
package eu.usrv.amdiforge.runnables;

import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RunnableManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static RunnableManager _mInstance;

    public static RunnableManager getInstance() {
        if (_mInstance == null) _mInstance = new RunnableManager();

        return _mInstance;
    }

    private RunnableManager() {
        scheduler.scheduleAtFixedRate(EntityCounter.getInstance(), 10, 10, SECONDS);
    }
}

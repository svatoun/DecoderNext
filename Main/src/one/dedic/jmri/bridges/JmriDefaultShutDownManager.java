/*
 * 
 * Portions Copyright (c) 2008-2019, Bob Jacobsen, JMRI; licensed under GPLv2, 
 * see http://www.jmri.org/ for the details.
 */
package one.dedic.jmri.bridges;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import org.openide.LifecycleManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shutdown manager implementation that cooperates with Netbeans infrastructure.
 *
 * @author JMRI
 * @author sdedic
 */
public class JmriDefaultShutDownManager implements ShutDownManager {
    private static volatile boolean shuttingDown = false;
    private final static Logger log = LoggerFactory.getLogger(JmriDefaultShutDownManager.class);
    private final ArrayList<ShutDownTask> tasks = new ArrayList<>();

    /**
     * Create a new shutdown manager.
     */
    public JmriDefaultShutDownManager() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void register(ShutDownTask s) {
        Objects.requireNonNull(s, "Shutdown task cannot be null.");
        if (!this.tasks.contains(s)) {
            this.tasks.add(s);
        } else {
            log.debug("already contains " + s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void deregister(ShutDownTask s) {
        if (s == null) {
            // silently ignore null task
            return;
        }
        if (this.tasks.contains(s)) {
            this.tasks.remove(s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShutDownTask> tasks() {
        return java.util.Collections.unmodifiableList(tasks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shutdown() {
        return shutdown(0, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean restart() {
        return shutdown(100, true);
    }

    protected boolean shutdown(int status, boolean exit) {
        if (!exit) {
            return false;
        }
        Lookup.getDefault().lookup(LifecycleManager.class).exit(status);
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
    
    void realShutdown() {
        if (!shuttingDown) {
            Date start = new Date();
            log.debug("Shutting down with {} tasks", this.tasks.size());
            setShuttingDown(true);
            long timeout = 30; // all shut down tasks must complete within n seconds
            // trigger parallel tasks (see jmri.ShutDownTask#isParallel())
            runShutDownTasks(true);
            log.debug("parallel tasks completed executing {} milliseconds after starting shutdown", new Date().getTime() - start.getTime());
            // trigger non-parallel tasks
            runShutDownTasks(false);
            log.debug("sequential tasks completed executing {} milliseconds after starting shutdown", new Date().getTime() - start.getTime());
            // wait for parallel tasks to complete
            synchronized (start) {
                while (new ArrayList<>(this.tasks).stream().anyMatch((task) -> (task.isParallel() && !task.isComplete()))) {
                    try {
                        start.wait(100);
                    } catch (InterruptedException ex) {
                        // do nothing
                    }
                    if ((new Date().getTime() - start.getTime()) > (timeout * 1000)) { // milliseconds
                        log.warn("Terminating without waiting for the following tasks to complete");
                        this.tasks.forEach((task) -> {
                            if (!task.isComplete()) {
                                log.warn("\t{}", task.getName());
                            }
                        });
                        break;
                    }
                }
            }
            // success
            log.debug("Shutdown took {} milliseconds.", new Date().getTime() - start.getTime());
            log.info("Normal termination complete");
            // and now terminate forcefully
        }
    }
    
    /**
     * Run registered shutdown tasks. Any Exceptions are logged and otherwise
     * ignored.
     *
     * @param isParallel true if parallel-capable shutdown tasks are to be run;
     *                   false if shutdown tasks that must be run sequentially
     *                   are to be run
     * @return true if shutdown tasks ran; false if a shutdown task aborted the
     *         shutdown sequence
     */
    private boolean runShutDownTasks(boolean isParallel) {
        // can't return out of a stream or forEach loop
        for (ShutDownTask task : new ArrayList<>(this.tasks)) {
            if (task.isParallel() == isParallel) {
                log.debug("Calling task \"{}\"", task.getName());
                Date timer = new Date();
                try {
                    setShuttingDown(task.execute()); // if a task aborts the shutdown, stop shutting down
                    if (!shuttingDown) {
                        log.info("Program termination aborted by \"{}\"", task.getName());
                        return false;  // abort early
                    }
                } catch (Exception e) {
                    log.error("Error during processing of ShutDownTask \"{}\"", task.getName(), e);
                } catch (Throwable e) {
                    // try logging the error
                    log.error("Unrecoverable error during processing of ShutDownTask \"{}\"", task.getName(), e);
                    log.error("Terminating abnormally");
                    // also dump error directly to System.err in hopes its more observable
                    System.err.println("Unrecoverable error during processing of ShutDownTask \"" + task.getName() + "\"");
                    System.err.println(e);
                    System.err.println("Terminating abnormally");
                    // forcably halt, do not restart, even if requested
                    Runtime.getRuntime().halt(1);
                }
                log.debug("Task \"{}\" took {} milliseconds to execute", task.getName(), new Date().getTime() - timer.getTime());
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isShuttingDown() {
        return shuttingDown;
    }

    /**
     * This method is static so that if multiple DefaultShutDownManagers are
     * registered, they are all aware of this state.
     *
     * @param state true if shutting down; false otherwise
     */
    protected static void setShuttingDown(boolean state) {
        shuttingDown = state;
        log.debug("Setting shuttingDown to {}", state);
    }

}

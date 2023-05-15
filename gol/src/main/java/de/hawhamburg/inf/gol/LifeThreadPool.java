package de.hawhamburg.inf.gol;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A pool of LifeThreads and a queue holding Runnables to be processed.
 *
 * @author Christian Lins
 */
public class LifeThreadPool {

    /* Unsynchronized Queue of Runnables */
    private final Queue<Runnable> tasks = new LinkedList<>();

    /* Number of threads managed by this pool */
    private final int numThreads;

    /* The collection of LifeThread instances forming this pool */
    private final LifeThread[] threads;

    public LifeThreadPool(int numThreads) {
        this.numThreads = numThreads;
        this.threads = new LifeThread[numThreads];
    }

    /**
     * This method will block until the queue of tasks has been emptied by the
     * running threads.
     *
     * @throws InterruptedException
     */
    public void barrier() throws InterruptedException {

        synchronized (tasks) {
            while (!tasks.isEmpty()) {
                Thread.sleep(100);
            }
        }

    }

    /**
     * Calls interrupt() on every thread in this pool.
     */
    public void interrupt() {

        // create a stream of LifeThreads
        Stream<Thread> _threads = Arrays.stream(threads);

        // call interrupt() on each life thread
        _threads.forEach(lt -> lt.interrupt());

    }

    /**
     * Waits for all tasks to finish and calls interrupt on every thread. This
     * method is identical to synchronized calling barrier() and interrupt().
     *
     * @throws InterruptedException
     */
    public void joinAndExit() throws InterruptedException {
        // ensure thread safety for tasks thus synchronizing barrier and interrupt
        synchronized (tasks) {
            barrier();
            interrupt();
        }
    }

    /**
     * Adds a task to the queue of this pool.
     *
     * @param task Runnable containing the work to be done
     */
    public void submit(Runnable task) {
        synchronized (tasks) {
            tasks.add(task);
        }
    }

    /**
     * Removes and returns the next task from the queue of this pool. This
     * method blocks if the queue is currently empty.
     *
     * @return Next task from the pool queue
     * @throws InterruptedException
     */
    // synchronize getting next task to ensure extraction from tasks is thread-safe
    public Runnable nextTask() throws InterruptedException {
        synchronized (tasks) {
            if (tasks.isEmpty()) {
                Thread.sleep(100);
            }

            Runnable next = tasks.poll();
            return next;
        }

    }

    /**
     * Start all threads in this pool.
     */
    public void start() {

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new LifeThread(this);
            threads[i].start();
        }

    }
}

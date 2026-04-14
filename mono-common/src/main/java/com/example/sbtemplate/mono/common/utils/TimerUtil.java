package com.example.sbtemplate.mono.common.utils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Utility class for measuring elapsed time with nanosecond precision.
 * <p>
 * Features:
 * <ul>
 *   <li>Start/stop semantics</li>
 *   <li>Query elapsed time while running or stopped</li>
 *   <li>Non-blocking getters (no waiting)</li>
 *   <li>Injectable time source for deterministic tests</li>
 * </ul>
 *
 * <pre>{@code
 * TimerUtil timer = TimerUtil.create().start();
 * // Do some work...
 * long ns = timer.getNanoseconds(); // works while running
 * timer.stop();
 * long ms = timer.getMilliseconds();
 * double s = timer.getSeconds();
 * }</pre>
 */
public class TimerUtil {

    /** Source of monotonic time in nanoseconds (defaults to System.nanoTime). */
    private final LongSupplier nanoTimeSource;

    /** Start time (nanoseconds) captured when start() is called. */
    private long startTime;

    /** Frozen elapsed time (nanoseconds) when the timer is stopped. */
    private long elapsedTime;

    /** Whether the timer is currently running. */
    private boolean running;

    /** Creates an instance that uses {@link System#nanoTime()} as its time source. */
    public static TimerUtil create() {
        return new TimerUtil(System::nanoTime);
    }

    /**
     * Creates an instance using a custom time source (intended for tests).
     *
     * @param nanoTimeSource supplier that returns the current time in nanoseconds
     * @return a new {@code TimerUtil}
     */
    public static TimerUtil create(LongSupplier nanoTimeSource) {
        return new TimerUtil(nanoTimeSource);
    }

    /**
     * Preferred constructor (use {@link #create()} or {@link #create(LongSupplier)}).
     * @param nanoTimeSource supplier that returns the current time in nanoseconds
     */
    public TimerUtil(LongSupplier nanoTimeSource) {
        this.nanoTimeSource = Objects.requireNonNull(nanoTimeSource, "nanoTimeSource");
        this.startTime = 0L;
        this.elapsedTime = 0L;
        this.running = false;
    }

    /**
     * Starts (or restarts) the timer and resets the accumulated elapsed time.
     * @return this, for chaining
     */
    public TimerUtil start() {
        this.startTime = nanoTimeSource.getAsLong();
        this.elapsedTime = 0L;
        this.running = true;
        return this;
    }

    /**
     * Stops the timer and freezes the elapsed time.
     * Calling this method multiple times is idempotent.
     * @return this, for chaining
     */
    public TimerUtil stop() {
        if (running) {
            this.elapsedTime = nanoTimeSource.getAsLong() - this.startTime;
            this.running = false;
        }
        return this;
    }

    /**
     * @return {@code true} if the timer is currently running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the elapsed time in nanoseconds.
     * If the timer is running, this is computed on the fly from the current time.
     * @return the elapsed time in nanoseconds
     */
    public long getNanoseconds() {
        return running ? (nanoTimeSource.getAsLong() - startTime) : elapsedTime;
    }

    /**
     * Returns the elapsed time in milliseconds (truncated).
     * @return the elapsed time in milliseconds
     */
    public long getMilliseconds() {
        return TimeUnit.NANOSECONDS.toMillis(getNanoseconds());
    }

    /**
     * Returns the elapsed time in seconds.
     * @return the elapsed time in seconds 3 decimals
     */
    public double getSeconds() {
        return Math.round((getNanoseconds() / 1_000_000_000.0) * 1000.0) / 1000.0;
    }
}

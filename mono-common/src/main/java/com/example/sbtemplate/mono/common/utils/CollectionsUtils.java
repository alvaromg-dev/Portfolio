package com.example.sbtemplate.mono.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility helpers for working with arrays and collections.
 * <p>
 * Focus areas:
 * - Lightweight array helpers to add or remove elements by returning new arrays.
 * - Partitioning lists into near-even parts while preserving order.
 * - Concurrent iteration/mapping of lists with stable output ordering.
 * </p>
 * <p>
 * Thread-safety: All methods are static and stateless. Methods that accept collections do not
 * mutate the provided inputs unless explicitly documented (e.g., {@link List#subList(int, int)} views).
 * </p>
 */
public class CollectionsUtils {

    // ==========================
    // Attributes
    // ==========================

    // ==========================
    // Constructors
    // ==========================

    /** Utility class; no instantiation. */
    private CollectionsUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    // ==========================
    // Methods
    // ==========================

    /**
     * Returns a new array with the given items appended at the end.
     *
     * @param array the source array; must not be {@code null}
     * @param items the items to append; may be {@code null} (treated as empty)
     * @param <T>   component type
     * @return a new array containing the original elements followed by the items
     * @throws NullPointerException if {@code array} is {@code null}
     */
    @SafeVarargs
    public static <T> T[] addLast(T[] array, T... items) {
        Objects.requireNonNull(array);
        int add = (items == null) ? 1 : items.length;
        if (add == 0) return array;
        int len = array.length;
        T[] out = Arrays.copyOf(array, len + add);
        for (int i = 0; i < add; i++) out[len + i] = (items == null ? null : items[i]);
        return out;
    }

    /**
     * Returns a new array with the given items inserted at the beginning.
     *
     * @param array the source array; must not be {@code null}
     * @param items the items to prepend; may be {@code null} (treated as empty)
     * @param <T>   component type
     * @return a new array containing the items followed by the original elements
     * @throws NullPointerException if {@code array} is {@code null}
     */
    @SafeVarargs
    public static <T> T[] addFirst(T[] array, T... items) {
        Objects.requireNonNull(array);
        int add = (items == null) ? 1 : items.length;
        if (add == 0) return array;
        int len = array.length;
        T[] out = Arrays.copyOf(array, len + add);
        System.arraycopy(array, 0, out, add, len);
        for (int i = 0; i < add; i++) out[i] = (items == null ? null : items[i]);
        return out;
    }

    /**
     * Returns a new array with the given items inserted starting at the specified index.
     *
     * @param array the source array; must not be {@code null}
     * @param index the zero-based index at which to insert; must be within {@code [0, array.length]}
     * @param items the items to insert; may be {@code null} (treated as empty)
     * @param <T>   component type
     * @return a new array with the items inserted at {@code index}
     * @throws NullPointerException      if {@code array} is {@code null}
     * @throws IndexOutOfBoundsException if {@code index} is out of bounds
     */
    @SafeVarargs
    public static <T> T[] addAt(T[] array, int index, T... items) {
        Objects.requireNonNull(array);
        int len = array.length;
        if (index < 0 || index > len) throw new IndexOutOfBoundsException("index=" + index);
        int add = items == null ? 1 : items.length;
        if (add == 0) return array;
        T[] out = Arrays.copyOf(array, len + add);
        if (index < len) System.arraycopy(array, index, out, index + add, len - index);
        for (int i = 0; i < add; i++) out[index + i] = (items == null ? null : items[i]);
        return out;
    }

    /**
     * Returns a new array with the last element removed.
     *
     * @param array the source array; must not be {@code null}
     * @param <T>   component type
     * @return a new array missing the last element
     * @throws NullPointerException     if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty
     */
    public static <T> T[] removeLast(T[] array) {
        Objects.requireNonNull(array);
        int len = array.length;
        if (len == 0) throw new IllegalArgumentException("array is empty");
        return Arrays.copyOf(array, len - 1);
    }

    /**
     * Returns a new array with the first element removed.
     *
     * @param array the source array; must not be {@code null}
     * @param <T>   component type
     * @return a new array missing the first element
     * @throws NullPointerException     if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is empty
     */
    public static <T> T[] removeFirst(T[] array) {
        Objects.requireNonNull(array);
        int len = array.length;
        if (len == 0) throw new IllegalArgumentException("array is empty");
        return Arrays.copyOfRange(array, 1, len);
    }

    /**
     * Returns a new array with the element at the given index removed.
     *
     * @param array the source array; must not be {@code null}
     * @param index the zero-based index to remove; must be within {@code [0, array.length)}
     * @param <T>   component type
     * @return a new array missing the element at {@code index}
     * @throws NullPointerException      if {@code array} is {@code null}
     * @throws IndexOutOfBoundsException if {@code index} is out of bounds
     */
    public static <T> T[] removeAt(T[] array, int index) {
        Objects.requireNonNull(array);
        int len = array.length;
        if (index < 0 || index >= len) throw new IndexOutOfBoundsException("index=" + index);
        T[] out = Arrays.copyOf(array, len - 1);
        if (index < len - 1) System.arraycopy(array, index + 1, out, index, len - index - 1);
        return out;
    }

    /**
     * Returns whether the array contains the same reference as {@code item}.
     * <p>
     * This method uses reference equality ({@code ==}), not {@link Object#equals(Object)}.
     * If you need value-based equality, prefer {@code Arrays.asList(array).contains(item)}.
     * </p>
     *
     * @param array the source array; must not be {@code null}
     * @param item  the reference to search for; must not be {@code null}
     * @param <T>   component type
     * @return {@code true} if any array element is the same reference as {@code item}
     * @throws NullPointerException if {@code array} or {@code item} is {@code null}
     */
    public static <T> boolean contains(T[] array, T item) {
        Objects.requireNonNull(array, "array must not be null");
        for (T nextArray : array) if (nextArray == item) return true;
        return false;
    }

    /**
     * Splits the given list into the specified number of parts, as evenly as possible,
     * while preserving the original order.
     * <p>
     * The absolute size difference between any two returned parts is at most one.
     * If {@code parts} is greater than the list size, some trailing parts will be empty.
     * </p>
     *
     * @param list  the source list to split; must not be {@code null}
     * @param parts the number of parts to produce; must be {@code >= 1}
     * @param <T>   element type
     * @return a list containing exactly {@code parts} sublists that partition the input
     *
     * @implNote Each returned sublist is a view backed by the original list via
     * {@link java.util.List#subList(int, int)}. Mutations on a sublist reflect in the
     * original list and vice versa. If independent copies are required, wrap the
     * sublists with {@link java.util.ArrayList#ArrayList(java.util.Collection)}.
     */
    public static <T> List<List<T>> splitIntoParts(
        List<T> list,
        int parts
    ) {
        Objects.requireNonNull(list, "list must not be null");
        if (parts < 1) throw new IllegalArgumentException("parts must be >= 1");

        // Compute sizes
        int listSize = list.size();
        int partsSize = listSize / parts;
        int remainder = listSize % parts;

        // Container for all parts
        List<List<T>> allParts = new ArrayList<>(parts);

        // Split the source list into (roughly) equal parts
        // (some parts may be one element larger when there is a remainder)
        int idx = 0;
        for (int i = 0; i < parts; i++) {
            int size = partsSize + (i < remainder ? 1 : 0);
            allParts.add(list.subList(idx, idx + size));
            idx += size;
        }

        // Return the partitioned list
        return allParts;
    }

    /**
     * Concurrently maps a list of items into a list of results while preserving order.
     * <p>
     * The input list is partitioned into up to {@code threads} chunks. Each chunk is processed
     * on a worker, and results are written into a pre-sized buffer to preserve the input order.
     * Exceptions from workers are rethrown on the calling thread.
     * </p>
     *
     * @param list     input elements; must not be {@code null}
     * @param threads  number of partitions/threads; must be {@code > 0}
     * @param function mapping function applied to each input element; must not be {@code null}
     * @param <T>      input element type
     * @param <R>      result element type
     * @return list with the results in the same order as the input
     * @throws IllegalArgumentException if {@code threads <= 0}
     * @throws IllegalStateException    if execution is interrupted
     * @throws Error                    if a worker throws an {@link Error}
     * @throws RuntimeException         if a worker throws a {@link RuntimeException}
     */
    public static <T, R> List<R> forEachInThreads(
        List<T> list,
        int threads,
        Function<? super T, ? extends R> function
    ) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(function, "function");
        if (list.isEmpty()) return Collections.emptyList();
        if (threads <= 0) throw new IllegalArgumentException("threads must be > 0");

        int workerCount = Math.min(threads, list.size());
        List<List<T>> partitions = splitIntoParts(list, workerCount);

        // Pre-allocate a results buffer of the same size to keep ordering stable.
        List<R> results = new ArrayList<>(Collections.nCopies(list.size(), null));

        ExecutorService pool = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
        try {
            List<Future<?>> futures = new ArrayList<>(workerCount);
            int offset = 0;
            for (List<T> part : partitions) {
                if (ObjectUtils.isNullOrEmpty(part)) continue;
                final int startIndex = offset;
                futures.add(pool.submit(() -> {
                    for (int i = 0; i < part.size(); i++) {
                        R r = function.apply(part.get(i));
                        results.set(startIndex + i, r);
                    }
                }));
                offset += part.size();
            }
            for (Future<?> f : futures) f.get();
            return results;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execution interrupted", ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if (cause instanceof Error e) throw e;
            if (cause instanceof RuntimeException re) throw re;
            throw new java.util.concurrent.CompletionException(cause);
        } finally {
            shutdownAndAwaitTermination(pool);
        }
    }

    /**
     * Concurrently runs an action on each element, partitioned across threads.
     * <p>
     * Each partition is processed sequentially within its worker, so the relative order of items
     * within a partition is preserved, while partitions themselves may run concurrently.
     * Exceptions from workers are rethrown on the calling thread.
     * </p>
     *
     * @param list     input elements; must not be {@code null}
     * @param threads  number of partitions/threads; must be {@code > 0}
     * @param consumer action to execute for each element; must not be {@code null}
     * @param <T>      input element type
     * @throws IllegalArgumentException if {@code threads <= 0}
     * @throws IllegalStateException    if execution is interrupted
     * @throws Error                    if a worker throws an {@link Error}
     * @throws RuntimeException         if a worker throws a {@link RuntimeException}
     */
    public static <T> void forEachInThreads(
        List<T> list,
        int threads,
        Consumer<? super T> consumer
    ) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(consumer, "consumer");
        if (list.isEmpty()) return;
        if (threads <= 0) throw new IllegalArgumentException("threads must be > 0");

        int workerCount = Math.min(threads, list.size());
        List<List<T>> partitions = splitIntoParts(list, workerCount);

        ExecutorService pool = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
        try {
            List<Future<?>> futures = new ArrayList<>(workerCount);
            for (List<T> part : partitions) {
                if (ObjectUtils.isNullOrEmpty(part)) continue;
                futures.add(pool.submit(() -> {
                    for (T item : part) {
                        consumer.accept(item);
                    }
                }));
            }
            for (Future<?> f : futures) f.get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execution interrupted", ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if (cause instanceof Error e) throw e;
            if (cause instanceof RuntimeException re) throw re;
            throw new java.util.concurrent.CompletionException(cause);
        } finally {
            shutdownAndAwaitTermination(pool);
        }
    }

    // ==========================
    // Private Helpers
    // ==========================

    /**
     * Attempts a graceful shutdown of the given executor using the recommended two-phase
     * termination pattern.
     * <p>
     * Behavior:
     * - First calls {@link ExecutorService#shutdown()} to reject new tasks and allow in-flight
     *   tasks to complete.
     * - Waits up to 30 seconds for termination.
     * - If still not terminated, calls {@link ExecutorService#shutdownNow()} to cancel pending
     *   tasks and interrupt workers, then waits up to 10 seconds more.
     * - If termination still does not happen, it gives up and returns.
     * </p>
     * <p>
     * Interruption: If the current thread is interrupted while awaiting termination, the method
     * invokes {@code shutdownNow()}, re-asserts the interrupted status with
     * {@link Thread#interrupt()}, and returns.
     * </p>
     * <p>
     * This method never throws checked exceptions and guarantees the executor ends in either the
     * {@code SHUTDOWN} or {@code TERMINATED} state.
     * </p>
     *
     * @param pool the executor to stop; must not be {@code null}
     * @see ExecutorService#shutdown()
     * @see ExecutorService#shutdownNow()
     * @see ExecutorService#awaitTermination(long, TimeUnit)
     */
    private static void shutdownAndAwaitTermination(ExecutorService pool) {
        Objects.requireNonNull(pool);
        pool.shutdown();
        try {
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                pool.awaitTermination(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

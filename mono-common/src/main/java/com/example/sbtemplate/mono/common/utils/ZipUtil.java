package com.example.sbtemplate.mono.common.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Stateful ZIP builder for creating ZIP archives either in memory
 * or directly to a provided {@link OutputStream}.
 *
 * <p>This class maintains internal state so you can add multiple files and then
 * close the archive when done. All timestamps are set to a fixed instant to
 * make output deterministic.</p>
 *
 * <h3>Typical usage (in-memory)</h3>
 * <pre>{@code
 *   try (ZipUtil zip = new ZipUtil(Deflater.DEFAULT_COMPRESSION)) {
 *      zip.addFile("hello/one.txt", "Hello 1!".getBytes(StandardCharsets.UTF_8));
 *      zip.addFile("hello/two.txt", "Hello 2!".getBytes(StandardCharsets.UTF_8));
 *
 *      byte[] bytes = zip.getBytes();
 *   } catch(IOException e) {
 *      e.printStackTrace();
 *   }
 * }</pre>
 *
 * <h3>Typical usage (to file)</h3>
 * <pre>{@code
 * File zipFile = new File("big.zip");
 *
 * try (FileOutputStream fos = new FileOutputStream(zipFile);
 *   ZipUtil zip = new ZipUtil(fos, Deflater.BEST_COMPRESSION)) {
 *
 *   // === 1) Add files from memory ===
 *   zip.addFile("folder/file1.txt", "This is a test text".getBytes(StandardCharsets.UTF_8));
 *
 *   // === 2) Add large files from disk ===
 *   // (these are streamed, not fully loaded into memory)
 *   zip.addFile("data/video.mp4", new File("input/video.mp4"));
 *   zip.addFile("data/backup.sql", new File("input/backup.sql"));
 *
 *   // === 3) Add lots of synthetic data ===
 *   StringBuilder sb = new StringBuilder();
 *   for (int i = 0; i < 5_000_000; i++) { // ~50 MB of text
 *      sb.append("Line ").append(i).append("\n");
 *   }
 *   zip.addFile("big/logs.txt", sb.toString().getBytes(StandardCharsets.UTF_8));
 *
 *   // When leaving try-with-resources it will close automatically
 *   } catch (Exception e) {
 *      e.printStackTrace();
 *   }
 * }</pre>
 *
 * <h3>Features</h3>
 * <ul>
 *   <li>Deterministic ZIPs (reproducible timestamps).</li>
 *   <li>Validates entry names to avoid path traversal or illegal characters.</li>
 *   <li>Automatically creates intermediate directories.</li>
 *   <li>Avoids duplicate entries.</li>
 *   <li>Supports adding from {@link InputStream}, {@code byte[]} and {@link File}.</li>
 * </ul>
 *
 * <p><strong>Note:</strong> In external OutputStream mode, {@link #getBytes()} and
 * {@link #toInputStream()} are not available.</p>
 */
public final class ZipUtil implements AutoCloseable {

    // ==========================
    // Attributes
    // ==========================

    /** Default buffer size for copying streams (8 KB). */
    private static final int BUFFER_SIZE = 8192;

    /** Reproducible file timestamp (Jan 1st, 1980, 00:00 UTC). */
    private static final FileTime REPRO_TIME = FileTime.from(
        LocalDateTime.of(1980, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)
    );

    /** Memory buffer holding the ZIP content (only for in-memory mode). */
    private final ByteArrayOutputStream memory;

    /** The ZIP stream to write entries to. */
    private final ZipOutputStream zip;

    /** Tracks created entries (files/dirs) to avoid duplicates. */
    private final Set<String> createdEntries = new HashSet<>();

    /** Closed flag to prevent double close / misuse. */
    private boolean closed = false;

    // ==========================
    // Constructors
    // ==========================

    /**
     * Creates a ZIP builder that writes to an in-memory buffer.
     *
     * @param compressionLevel compression level (-1..9)  (-1 = DEFAULT)
     * @throws IllegalArgumentException if the compression level is invalid
     */
    public ZipUtil(int compressionLevel) {
        validateCompressionLevel(compressionLevel);

        this.memory = new ByteArrayOutputStream();
        this.zip = new ZipOutputStream(this.memory, StandardCharsets.UTF_8);
        this.zip.setLevel(compressionLevel);
    }

    /**
     * Creates a ZIP builder that writes to a provided {@link OutputStream}.
     *
     * <p>In this mode, {@link #getBytes()} and {@link #toInputStream()} are NOT available.</p>
     *
     * @param out              the destination OutputStream (e.g. FileOutputStream)
     * @param compressionLevel compression level (-1..9)
     * @throws IllegalArgumentException if the compression level is invalid
     */
    public ZipUtil(OutputStream out, int compressionLevel) {
        validateCompressionLevel(compressionLevel);
        Objects.requireNonNull(out, "OutputStream must not be null");

        this.memory = null; // not used in this mode
        this.zip = new ZipOutputStream(out, StandardCharsets.UTF_8);
        this.zip.setLevel(compressionLevel);
    }

    // ==========================
    // Public methods
    // ==========================

    /**
     * Adds data from an {@link InputStream} as a ZIP entry.
     * The provided input stream is <strong>not</strong> closed by this method.
     */
    public void addFile(String entryName, InputStream data) throws IOException {
        ensureOpenForWrite();
        Objects.requireNonNull(data, "in must not be null");

        entryName = prepareEntryName(entryName);
        ensureParentDirectories(entryName);
        putNewEntry(entryName);

        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = data.read(buf)) != -1) {
                zip.write(buf, 0, n);
            }
        } finally {
            zip.closeEntry();
        }
    }

    /** Adds a byte array as a ZIP entry. */
    public void addFile(String entryName, byte[] data) throws IOException {
        ensureOpenForWrite();
        Objects.requireNonNull(data, "data must not be null");
        try (InputStream in = new ByteArrayInputStream(data)) {
            addFile(entryName, in);
        }
    }

    /** Adds a disk file as a ZIP entry (streams the file content). */
    public void addFile(String entryName, File data) throws IOException {
        ensureOpenForWrite();
        Objects.requireNonNull(data, "file must not be null");
        try (InputStream in = new BufferedInputStream(new FileInputStream(data))) {
            addFile(entryName, in);
        }
    }

    /**
     * Closes (finalizes) the ZIP. After closing, entries can no longer be added.
     * In memory mode, {@link #getBytes()} and {@link #toInputStream()} will
     * continue to work after close.
     */
    @Override
    public void close() throws IOException {
        if (closed) return;
        closed = true;
        zip.close(); // writes central directory and finalizes the archive
    }

    /**
     * Returns the generated ZIP bytes (in-memory mode only).
     * <p>If the ZIP is not closed yet, this method will finalize it automatically.</p>
     */
    public byte[] getBytes() throws IOException {
        ensureInMemoryMode();
        ensureFinalized();
        return memory.toByteArray();
    }

    /**
     * Returns an {@link InputStream} over the generated ZIP (in-memory mode only).
     * <p>If the ZIP is not closed yet, this method will finalize it automatically.</p>
     */
    public InputStream toInputStream() throws IOException {
        ensureInMemoryMode();
        ensureFinalized();
        return new ByteArrayInputStream(memory.toByteArray());
    }

    /** Returns whether this ZIP has been closed. */
    public boolean isClosed() {
        return closed;
    }

    // ==========================
    // Internal helpers
    // ==========================

    /** Validates the compression level is within -1..9. */
    private static void validateCompressionLevel(int level) {
        if (level < Deflater.DEFAULT_COMPRESSION || level > Deflater.BEST_COMPRESSION) {
            throw new IllegalArgumentException("compressionLevel must be between -1 and 9");
        }
    }

    /** Ensures this instance is still open for writing entries. */
    private void ensureOpenForWrite() {
        if (closed) throw new IllegalStateException("ZipUtil is already closed");
    }

    /** Ensures we are in memory mode (not OutputStream mode). */
    private void ensureInMemoryMode() {
        if (memory == null) {
            throw new UnsupportedOperationException(
                "This ZipUtil was created with an external OutputStream; no bytes available"
            );
        }
    }

    /**
     * Ensures the ZIP is finalized (central directory written).
     * If not yet closed and we're in-memory, we close it now.
     */
    private void ensureFinalized() throws IOException {
        if (!closed) {
            close(); // safe, idempotent
        }
    }

    /** Prepares and validates an entry name (normalizes slashes, strips leading /). */
    private String prepareEntryName(String raw) {
        Objects.requireNonNull(raw, "entryName must not be null");
        String name = raw.replace('\\', '/').replaceAll("^/+", "");
        validateEntryName(name);
        return name;
    }

    /** Validates entry name for security (no ../, no null chars, not blank). */
    private static void validateEntryName(String name) {
        if (name.isBlank() || name.contains("../") || name.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Illegal entry name: " + name);
        }
    }

    /** Ensures all parent directories exist inside the ZIP before adding a file. */
    private void ensureParentDirectories(String entryName) throws IOException {
        int idx = entryName.lastIndexOf('/');
        if (idx <= 0) return;

        String[] parts = entryName.substring(0, idx).split("/");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(p).append('/');
            addDirectoryIfNeeded(sb.toString());
        }
    }

    /** Adds a directory entry if not already present. */
    private void addDirectoryIfNeeded(String dirName) throws IOException {
        String normalized = dirName.replace('\\', '/');
        if (!normalized.endsWith("/")) normalized = normalized + "/";

        if (createdEntries.add(normalized)) {
            ZipEntry dirEntry = new ZipEntry(normalized);
            setReproducibleTimes(dirEntry);
            zip.putNextEntry(dirEntry);
            zip.closeEntry();
        }
    }

    /** Creates and positions the ZIP stream at a new file entry. */
    private void putNewEntry(String entryName) throws IOException {
        if (!createdEntries.add(entryName)) {
            throw new IOException("Duplicate ZIP entry: " + entryName);
        }
        ZipEntry entry = new ZipEntry(entryName);
        setReproducibleTimes(entry);
        zip.putNextEntry(entry);
    }

    /** Applies reproducible timestamps to a ZIP entry. */
    private static void setReproducibleTimes(ZipEntry entry) {
        entry.setLastModifiedTime(REPRO_TIME);
        entry.setCreationTime(REPRO_TIME);
        entry.setLastAccessTime(REPRO_TIME);
    }
}

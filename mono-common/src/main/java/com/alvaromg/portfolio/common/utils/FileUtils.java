package com.alvaromg.portfolio.common.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.regex.Pattern;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    // ==========================
    // Attributes
    // ==========================

    // ==========================
    // Constructors
    // ==========================

    /**
     * Utility class; no instantiation.
     */
    private FileUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    // ==========================
    // Methods
    // ==========================

    /**
     * Reads all bytes from the given file.
     *
     * @param file the file to read
     * @return a byte array containing the file's content
     * @throws IOException              if an I/O error occurs while reading the
     *                                  file
     * @throws IllegalArgumentException if the file is null, does not exist, or is
     *                                  not a regular file
     */
    public static byte[] readBytes(File file) throws IOException {

        // Validate inputs
        Objects.requireNonNull(file, "File must not be null");

        // Check if the file exists and is a regular file
        if (!safeExists(file) || file.isDirectory())
            throw new IllegalArgumentException("File must exist and be a regular file: " + file);

        // Read all bytes from the file and return them
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Opens a new {@link InputStream} for the given file.
     * The caller is responsible for closing the returned stream.
     * @param file the file to read
     * @return a InputStream containing the file's content
     * @throws IOException              if an I/O error occurs while reading the
     *                                  file
     * @throws IllegalArgumentException if the file is null, does not exist, or is
     *                                  not a regular file
     */
    public static InputStream readInputStream(File file) throws IOException {

        // Validate inputs
        Objects.requireNonNull(file, "File must not be null");

        // Check if the file exists and is a regular file
        if (!safeExists(file) || file.isDirectory())
            throw new IllegalArgumentException("File must exist and be a regular file: " + file);

        // Open and return the stream; caller must close it
        return Files.newInputStream(file.toPath());
    }

    /**
     * Reads a text file into a String using the provided charset.
     *
     * @param file the file to read (must exist and be a regular file)
     * @param charset the charset to use (must not be null)
     * @return the file content as a String
     * @throws IOException              if an I/O error occurs while reading the
     *                                  file
     * @throws IllegalArgumentException if the file is null, does not exist, or is
     *                                  not a regular file, or if charset is null
     */
    public static String readString(File file, Charset charset) throws IOException {

        // Validate inputs
        Objects.requireNonNull(file, "File must not be null");
        Objects.requireNonNull(charset, "Charset must not be null");

        // Read the file and convert bytes to String using the specified charset
        return bytesToString(readBytes(file), charset);
    }

    /**
     * Reads a text file into a String using UTF-8.
     *
     * @param file the file to read (must exist and be a regular file)
     * @return the content of the file as a String
     * @throws IOException              if an I/O error occurs while reading the
     *                                  file
     * @throws IllegalArgumentException if the file is null, does not exist, or is
     *                                  not a regular file
     */
    public static String readUtf8(File file) throws IOException {
        return readString(file, StandardCharsets.UTF_8);
    }

    /**
     * Saves the content of an {@link InputStream} to the given destination file.
     * Parent directories are created when missing.
     *
     * @param data     the InputStream to save
     * @param target the file to save the content to
     * @throws IOException              if an I/O error occurs while writing to the
     *                                  file
     * @throws IllegalArgumentException if content or destination is null
     */
    public static boolean write(InputStream data, File target, boolean overwrite) throws IOException {

        // Validate inputs
        Objects.requireNonNull(data, "InputStream must not be null");
        Objects.requireNonNull(target, "Target file must not be null");

        // Ensure the parent directories of the destination file exist
        createParentDirectories(target);

        // Copy the content from the InputStream to the destination file
        if (overwrite) {
            Files.copy(data, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            if (safeExists(target)) {
                throw new IOException("Target file already exists: " + target);
            }
            Files.copy(data, target.toPath());
        }

        // Check the files was saved
        return safeExists(target);
    }

    /**
     * Saves the content of a byte array to the given destination file.
     * Parent directories are created when missing.
     *
     * @param data     the byte array to save
     * @param target the file to save the content to
     * @throws IOException              if an I/O error occurs while writing to the
     *                                  file
     * @throws IllegalArgumentException if content or destination is null
     */
    public static boolean write(byte[] data, File target, boolean overwrite) throws IOException {

        // Validate inputs
        Objects.requireNonNull(data, "InputStream must not be null");
        Objects.requireNonNull(target, "Target file must not be null");

        // Ensure the parent directories of the destination file exist
        return write(new ByteArrayInputStream(data), target, overwrite);
    }

    /**
     * Deletes the file if it exists.
     *
     * @param file the file to delete
     * @return true if the file was deleted, false if it did not exist
     * @throws IllegalArgumentException if the file is null
     * @throws IOException              if an I/O error occurs while deleting the
     *                                  file
     */
    public static boolean delete(File file) throws IOException {

        // Validate inputs
        Objects.requireNonNull(file, "File must not be null");

        // Delete
        return Files.deleteIfExists(file.toPath());
    }

    /**
     * Renames the {@code source} file to {@code newName} under the same parent
     * directory (by default).
     * If {@code newName} contains subfolders, they are resolved relative to the
     * source's parent.
     * Overwrites if the target exists.
     *
     * @param source  the file to rename
     * @param newName the new name for the file (can include subfolders)
     * @return true if the rename was successful, false otherwise
     * @throws IllegalArgumentException if source is null or newName is empty
     * @throws IOException              if an I/O error occurs while renaming the
     *                                  file
     */
    public static boolean rename(File source, String newName) throws IOException {

        // Validate inputs
        Objects.requireNonNull(source, "Source file must not be null");
        Objects.requireNonNull(newName, "New name must not be null");

        // Check if the source file exists and is a regular file
        File parent = source.getParentFile();
        File target = (parent == null) ? new File(newName) : new File(parent, newName);

        // Move the source file to the target location, overwriting if necessary
        return move(source, target, true);
    }

    /**
     * Copies a file to a target path.
     *
     * @param source    the source file to copy
     * @param target    the target file path where the source will be copied
     * @param overwrite whether to replace the target if it already exists
     * @throws IllegalArgumentException if source or target is null
     * @throws IOException              if an I/O error occurs while copying the
     *                                  file
     */
    public static boolean copy(File source, File target, boolean overwrite) throws IOException {

        // Validate inputs
        Objects.requireNonNull(source, "Source file must not be null");
        Objects.requireNonNull(target, "Target file must not be null");
        if (!safeExists(source) || source.isDirectory())
            throw new IllegalArgumentException("Source must exist and be a regular file: " + source);

        // Check if the source file exists and is a regular file
        createParentDirectories(target);

        // Copy the source file to the target location
        if (overwrite) {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            if (safeExists(target)) {
                throw new IOException("Target file already exists: " + target);
            }
            Files.copy(source.toPath(), target.toPath());
        }

        // Check the files was saved
        return safeExists(target);
    }

    /**
     * Moves or renames a file.
     *
     * @param source    the source file to move
     * @param target    the target file path where the source will be moved
     *                  (can include subfolders, resolved relative to the source's
     *                  parent)
     * @param overwrite whether to replace the target if it already exists
     * @return true if the move completed
     * @throws IllegalArgumentException if source or target is null
     * @throws IOException              if an I/O error occurs while moving the file
     * @throws IOException              if the target file already exists and
     *                                  overwrite is false
     */
    public static boolean move(File source, File target, boolean overwrite) throws IOException {

        // Validate inputs
        Objects.requireNonNull(source, "Source file must not be null");
        Objects.requireNonNull(target, "Target file must not be null");
        if (!safeExists(source) || source.isDirectory())
            throw new IllegalArgumentException("Source must exist and be a regular file: " + source);

        // Check if the source file exists and is a regular file
        createParentDirectories(target);

        // Move the source file to the target location, overwriting if necessary
        try {
            if (overwrite) {
                Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                if (safeExists(target)) throw new IOException("Target file already exists: " + target);
                Files.move(source.toPath(), target.toPath());
            }
        } catch (IOException ex) {
            // Fallback cross-filesystem
            copy(source, target, overwrite);
            Files.delete(source.toPath());
        }

        // Return true if the move was successful
        return safeExists(target);
    }

    /**
     * Returns the size of the given file in bytes.
     *
     * @param file the file to check
     * @return the size of the file in bytes
     * @throws IllegalArgumentException if the file does not exist or is a directory
     */
    public static long getSize(File file) {

        // Validate inputs
        if (!safeExists(file) || file.isDirectory())
            throw new IllegalArgumentException("File does not exist or is a directory: " + file);

        // Return the size of the file in bytes
        return file.length();
    }

    /**
     * Returns the size of the given ByteArrayOutputStream in bytes.
     *
     * @param baos the ByteArrayOutputStream to check
     * @return the size of the ByteArrayOutputStream in bytes
     * @throws IllegalArgumentException if baos is null
     */
    public static long getSize(ByteArrayOutputStream baos) {

        // Validate inputs
        Objects.requireNonNull(baos, "ByteArrayOutputStream must not be null");

        // Return size
        return baos.size();
    }

    /**
     * Returns whether the given file exists on disk.
     * Returns false if {@code file} is null.
     *
     * @param file the file to check
     * @return true if the file exists, false otherwise
     */
    public static boolean safeExists(File file) {
        return file != null &&
               file.exists();
    }

    /**
     * Returns the file creation timestamp as a {@link LocalDateTime} in the system
     * default zone.
     *
     * @param file the file to check
     * @return the creation time as a LocalDateTime
     * @throws IOException              if an I/O error occurs while accessing the
     *                                  file attributes
     * @throws IllegalArgumentException if the file is null
     */
    public static LocalDateTime getCreationTime(File file) throws IOException {

        // Validate inputs
        Objects.requireNonNull(file, "File must not be null");

        // Validate file
        if (!safeExists(file) || file.isDirectory())
            throw new IllegalArgumentException("File must exist and be a regular file: " + file);

        // Check if the file exists and is a regular file
        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

        // Return the creation time as a LocalDateTime in the system default zone
        return LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault());
    }

    /**
     * Returns the last modification timestamp as a {@link LocalDateTime} in the
     * system default zone.
     *
     * @param file the file to check
     * @return the last modification time as a LocalDateTime
     * @throws IOException              if an I/O error occurs while accessing the
     *                                  file attributes
     * @throws IllegalArgumentException if the file is null
     */
    public static LocalDateTime getLastModifiedTime(File file) throws IOException {

        // Validate inputs
        Objects.requireNonNull(file, "File must not be null");

        // Validate file
        if (!safeExists(file) || file.isDirectory())
            throw new IllegalArgumentException("File must exist and be a regular file: " + file);

        // Check if the file exists and is a regular file
        return LocalDateTime.ofInstant(Files.getLastModifiedTime(file.toPath()).toInstant(), ZoneId.systemDefault());
    }

    /**
     * Returns the last access timestamp as a {@link LocalDateTime} in the system
     * default zone.
     *
     * @param file the file to check
     * @return the last access time as a LocalDateTime
     * @throws IOException              if an I/O error occurs while accessing the
     *                                  file attributes
     * @throws IllegalArgumentException if the file is null
     */
    public static LocalDateTime getLastAccessTime(File file) throws IOException {

        // Validate inputs
        Objects.requireNonNull(file, "File must not be null");

        // Validate file
        if (!safeExists(file) || file.isDirectory())
            throw new IllegalArgumentException("File must exist and be a regular file: " + file);

        // Check if the file exists and is a regular file
        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

        // Return the last access time as a LocalDateTime in the system default zone
        return LocalDateTime.ofInstant(attrs.lastAccessTime().toInstant(), ZoneId.systemDefault());
    }

    /**
     * Ensures the parent directory of the provided file exists, creating it if
     * necessary.
     *
     * @param file the file whose parent directory should be created
     * @return true if the parent directory exists (or there was no parent), false
     *         otherwise
     * @throws IOException              if an I/O error occurs while creating the
     *                                  parent directory
     * @throws IllegalArgumentException if the file is null
     */
    public static boolean createParentDirectories(File file) throws IOException {

        // Validate inputs
        Objects.requireNonNull(file, "File must not be null");

        // Check if the file has a parent directory
        File parent = file.getParentFile();

        // If there is no parent directory, nothing to create
        if (parent == null) return true; // nothing to create

        // If parent exists, ensure it's a directory
        if (parent.exists()) {
            if (!parent.isDirectory())
                throw new IOException("Parent exists but is not a directory: " + parent);
            return true;
        }

        // Create the parent directory if it does not exist
        if (!parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Unable to create parent directories for: " + file);
        }

        // Return true if the parent directory exists or was created successfully
        return safeExists(parent);
    }

    /**
     * Filters CSV rows by applying a regex to the given column.
     * Empty lines and lines starting with '#' are ignored.
     *
     * @param csv            full CSV content
     * @param delimiter      column delimiter (literal, not regex; e.g. "," or "|")
     * @param column         zero-based column index to test
     * @param regex          regex to match against the cell content
     * @param discardMatches if true, rows whose cell matches are discarded;
     *                       otherwise kept
     * @return the filtered CSV, without a trailing newline
     * @throws IllegalArgumentException if inputs are invalid
     */
    @SuppressWarnings("java:S135")
    public static String filterCsvFile(
        String csv,
        String delimiter,
        int column,
        String regex,
        boolean discardMatches
    ) {
        // Validate inputs
        if (csv == null
                || csv.isEmpty()
                || delimiter == null || delimiter.isEmpty()
                || column < 0
                || regex == null || regex.isEmpty()) {
            throw new IllegalArgumentException("Invalid input parameters for CSV filtering");
        }

        // Split the CSV into rows and process each row
        final String[] rows = csv.split("\\r?\\n");
        final String delim = Pattern.quote(delimiter);
        final Pattern pattern = Pattern.compile(regex);
        StringBuilder out = new StringBuilder();

        // Iterate over each row, filtering based on the regex match in the specified
        // column
        for (String row : rows) {
            // Trim the row and skip empty lines or comments
            String trimmed = row.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            // Split the row by the delimiter and check the specified column
            String[] cells = row.split(delim, -1);
            if (column >= cells.length) continue;

            // Check if the cell matches the regex and append to output if it does not match
            // (or does match, based on discardMatches)
            boolean matches = pattern.matcher(cells[column]).matches();
            if (matches != discardMatches) {
                out.append(row).append('\n');
            }
        }

        // remove the trailing newline if present
        if (!out.isEmpty()) out.setLength(out.length() - 1);

        // Return the filtered CSV content
        return out.toString();
    }

    /**
     * Converts a byte array to a String using the provided charset.
     *
     * @param bytes the byte array to convert
     * @param charset the charset to use for conversion
     * @return the converted String
     * @throws IllegalArgumentException if charset or bytes are null
     */
    public static String bytesToString(byte[] bytes, Charset charset) {

        // Validate inputs
        Objects.requireNonNull(bytes, "Byte array must not be null");
        Objects.requireNonNull(charset, "Charset must not be null");

        // Convert bytes to string
        return new String(bytes, charset);
    }

    /**
     * Converts a byte array to a UTF-8 String.
     *
     * @param bytes the byte array to convert
     * @return the converted String
     * @throws IllegalArgumentException if bytes is null
     */
    @SuppressWarnings("java:S5669")
    public static String bytesToUtf8String(byte[] bytes) {

        // Validate inputs
        Objects.requireNonNull(bytes, "Byte array must not be null");

        // Convert the byte array to a String using UTF-8 encoding
        return bytesToString(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Returns the extension of a file name.
     *
     * @param fileName the file name to check
     * @return the file extension, or an empty string if the file name is null,
     *         empty, or has no extension
     */
    public static String fileExtension(String fileName) {

        // Validate inputs
        if (ObjectUtils.isNullOrEmpty(fileName))
            return "";

        // cut at first ? or #
        int q = fileName.indexOf('?');
        int h = fileName.indexOf('#');
        int cut;
        if (q >= 0 && h >= 0) {
            cut = Math.min(q, h);
        } else if (q >= 0) {
            cut = q;
        } else {
            cut = h;
        }
        if (cut != -1)
            fileName = fileName.substring(0, cut);

        // isolate base name
        int lastSep = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        String name = fileName.substring(lastSep + 1);
        if (name.isEmpty())
            return "";

        // Get the last dot
        int dot = name.lastIndexOf('.');

        // get the extension
        return (dot > 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
    }

    /**
     * Returns the extension of a file object.
     *
     * @param file the file to check
     * @return the file extension, or an empty string if the file is a directory or
     *         has no extension
     * @throws IllegalArgumentException if the file is null
     */
    public static String fileExtension(File file) {

        // Validate inputs
        Objects.requireNonNull(file, "File must not be null");

        // If the file is a directory, return an empty string
        return fileExtension(file.getName());
    }

    /**
     * Returns a new {@link InputStream} that reads the current contents of the provided
     * {@link ByteArrayOutputStream}.
     *
     * <p>Both streams remain open. No data is copied after this call; the returned
     * {@link ByteArrayInputStream} is backed by a new byte array snapshot of the BAOS.</p>
     *
     * <p><b>Memory note:</b> the entire content is materialized in memory.</p>
     *
     * @param out the {@link ByteArrayOutputStream} to read from (must not be null)
     * @return a new {@link ByteArrayInputStream} containing the data
     * @throws NullPointerException if {@code out} is null
     */
    public static InputStream outputStreamToInputStream(ByteArrayOutputStream out) {
        Objects.requireNonNull(out, "ByteArrayOutputStream must not be null");
        byte[] data = out.toByteArray(); // snapshot
        return new ByteArrayInputStream(data);
    }

    /**
     * Copies all data from the given {@link InputStream} into a new {@link ByteArrayOutputStream}.
     * The input stream is closed automatically.
     *
     * <p><b>Memory note:</b> the entire content is materialized in memory.</p>
     *
     * @param in the {@link InputStream} to copy from; it will be closed even if an exception occurs
     * @return a new {@link ByteArrayOutputStream} containing the copied data
     * @throws IOException if an I/O error occurs while reading
     * @throws NullPointerException if {@code in} is null
     */
    public static OutputStream inputStreamToOutputStream(InputStream in) throws IOException {
        Objects.requireNonNull(in, "InputStream must not be null");
        try (InputStream src = in;) {
            var out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192]; // 8 KB
            int bytesRead;
            while ((bytesRead = src.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out;
        }
    }
}

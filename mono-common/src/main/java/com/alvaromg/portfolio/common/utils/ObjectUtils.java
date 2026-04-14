package com.alvaromg.portfolio.common.utils;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import java.lang.reflect.Array;
import java.nio.Buffer;

/**
 * Utility methods for working with generic objects.
 * Provides null-safe equality checks, deep and shallow copy operations,
 * safe casting, and general-purpose null/empty checks for common types.
 * <p>
 * All methods are static and stateless, making this class thread-safe.
 * </p>
 */
public class ObjectUtils {

    // ==========================
    // Attributes
    // ==========================

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ==========================
    // Constructors
    // ==========================

    /**
     * Utility class; no instantiation.
     */
    private ObjectUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    // ==========================
    // Methods
    // ==========================

    /**
     * Null-safe equality comparison between two objects using
     * {@link Object#equals(Object)}.
     *
     * @param a first object
     * @param b second object
     * @return true if both are equal or both null, false otherwise
     */
    public static <T> boolean safeEquals(T a, T b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * Null-safe comparison for {@link Comparable} objects.
     * Null is considered less than non-null. Two nulls are considered equal.
     *
     * @param a first object
     * @param b second object
     * @return negative/zero/positive
     * @param <T> the type of the objects being compared, must implement Comparable
     */
    public static <T extends Comparable<? super T>> int safeCompareTo(T a, T b) {

        // If both have the same reference they are equal
        if (a == b) return 0;

        // If one is null and the other is not, the null one is considered less
        if (a == null) return -1;
        if (b == null) return +1;

        // Compare the two objects using their compareTo method
        return a.compareTo(b);
    }

    /**
     * Validates that the provided objects are neither null nor empty (after
     * trimming).
     * Supported types:
     * - CharSequence:   true if: null or length = 0 (trimmed)
     * - Collection:     true if: null or length = 0
     * - Array:          true if: null or length = 0
     * - Map:            true if: null or length = 0
     * - Optional:       true if: null or is empty
     * - OptionalInt:    true if: null or is empty
     * - OptionalLong:   true if: null or is empty
     * - OptionalDouble: true if: null or is empty
     * - Iterable:       true if: null or no elements
     * - Iterator:       true if: null or no elements
     * - Enumeration:    true if: null or no more elements
     * - Buffer:         true if: null or remaining == 0
     * - Other types:    true if: null
     * Throws {@link IllegalArgumentException} if validation fails.
     *
     * @param object the object to check
     * @param message the exception message to use if the check fails
     * @throws IllegalArgumentException if {@code objects} is null or empty
     * @throws NullPointerException if {@code object} is null
     */
    public static <T> T requireNonNullOrEmpty(T object, String message) {
        if (object == null) throw new NullPointerException(message);
        if (isNullOrEmpty(object)) throw new IllegalArgumentException(message);
        return object;
    }

    /**
     * Checks if the given objects is null or empty.
     * Supported types:
     * - CharSequence:   true if: null or length = 0 (trimmed)
     * - Collection:     true if: null or length = 0
     * - Array:          true if: null or length = 0
     * - Map:            true if: null or length = 0
     * - Optional:       true if: null or is empty
     * - OptionalInt:    true if: null or is empty
     * - OptionalLong:   true if: null or is empty
     * - OptionalDouble: true if: null or is empty
     * - Iterable:       true if: null or no elements
     * - Iterator:       true if: null or no elements
     * - Enumeration:    true if: null or no more elements
     * - Buffer:         true if: null or remaining == 0
     * - Other types:    true if: null
     *
     * @param objects the objects to check
     * @return true if the objects are null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(Object... objects) {

        // Check the list
        if (isSingleNullOrEmpty(objects)) return true;

        // Check all objects
        for (Object object : objects)
            if (isSingleNullOrEmpty(object)) return true;

        // Other types
        return false;
    }

    /**
     * Checks if the given object is null or empty.
     * Supported types:
     * - CharSequence:   true if: null or length = 0 (trimmed)
     * - Collection:     true if: null or length = 0
     * - Array:          true if: null or length = 0
     * - Map:            true if: null or length = 0
     * - Optional:       true if: null or is empty
     * - OptionalInt:    true if: null or is empty
     * - OptionalLong:   true if: null or is empty
     * - OptionalDouble: true if: null or is empty
     * - Iterable:       true if: null or no elements
     * - Iterator:       true if: null or no elements
     * - Enumeration:    true if: null or no more elements
     * - Buffer:         true if: null or remaining == 0
     * - Other types:    true if: null
     *
     * @param object the object to check
     * @return true if the object is null or empty, false otherwise
     */
    public static boolean isSingleNullOrEmpty(Object object) {
        if (object == null) return true;
        if (object instanceof CharSequence in)   return in.toString().trim().isEmpty();
        if (object instanceof Collection<?> in)  return in.isEmpty();
        if (object.getClass().isArray())         return Array.getLength(object) == 0;
        if (object instanceof Map<?, ?> in)      return in.isEmpty();
        if (object instanceof Optional<?> in)    return in.isEmpty() || isSingleNullOrEmpty(in.orElse(null));
        if (object instanceof OptionalInt in)    return in.isEmpty();
        if (object instanceof OptionalLong in)   return in.isEmpty();
        if (object instanceof OptionalDouble in) return in.isEmpty();
        if (object instanceof Iterable<?> in)    return !in.iterator().hasNext();
        if (object instanceof Iterator<?> in)    return !in.hasNext();
        if (object instanceof Enumeration<?> in) return !in.hasMoreElements();
        if (object instanceof Buffer in)         return in.remaining() == 0;
        return false;
    }

    /**
     * Attempts to cast an object to the specified type, returning null if
     * incompatible.
     * This method is useful for safely casting objects without throwing a
     * ClassCastException.
     *
     * @param obj   the object to cast
     * @param clazz the class to cast to
     * @param <T>   the type of the target class
     * @return the casted object if compatible, or null if not
     */
    public static <T> T safeCast(Object obj, Class<T> clazz) {

        // Validate inputs
        Objects.requireNonNull(clazz, "Target class must not be null");

        // If the object is null, return null
        if (obj == null) return null;

        // Check if the object is an instance of the specified class and cast it
        // If not, return null
        return clazz.isInstance(obj) ? clazz.cast(obj) : null;
    }

    /**
     * Performs a deep copy by serializing the source object to JSON and
     * deserializing it
     * into the provided target class.
     *
     * @param object the source object to copy (may be null)
     * @param clazz  the target class
     * @param <T>    the type of the target
     * @return a deep-copied instance, or {@code null} if {@code object} is null
     * @throws IllegalArgumentException if {@code clazz} is null
     */
    public static <T> T deepCopy(T object, Class<T> clazz) {

        // If the object is null, return null
        if (object == null) return null;

        // Validate inputs
        Objects.requireNonNull(clazz, "Target class must not be null");

        // Deserialize the JSON string back into an object of the specified class
        return MAPPER.convertValue(object, clazz);
    }

    /**
     * Performs a deep copy for generic targets using a {@link TypeReference}.
     * Example:
     *
     * <pre>
     * List<Foo> copy = ObjectUtils.deepCopy(originalList, new TypeReference<List<Foo>>() {
     * });
     * </pre>
     *
     * @param object  the source object to copy (may be null)
     * @param typeRef the Jackson {@code TypeReference} describing the target type
     * @param <T>     the type of the target
     * @return a deep-copied instance, or {@code null} if {@code object} is null
     * @throws IllegalArgumentException if {@code typeRef} is null
     */
    public static <T> T deepCopy(Object object, TypeReference<T> typeRef) {

        // If the object is null, return null
        if (object == null) return null;

        // Validate inputs
        Objects.requireNonNull(typeRef, "Target type reference must not be null");

        // Deserialize the JSON string back into an object of the specified type
        return MAPPER.convertValue(object, typeRef);
    }

    /**
     * Converts an object to a JSON string.
     *
     * @param object the object to convert (may be null)
     * @return the JSON string representation, or "null" if the object is null
     * @throws IllegalStateException if the object cannot be serialized to JSON
     */
    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize object to JSON: " + 
                    (object != null ? object.getClass().getName() : "null"), e);
        }
    }

    /**
     * Converts an object to a pretty-printed JSON string.
     *
     * @param object the object to convert (may be null)
     * @return the formatted JSON string representation
     * @throws IllegalStateException if the object cannot be serialized to JSON
     */
    public static String toJsonPretty(Object object) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize object to pretty JSON: " + 
                    (object != null ? object.getClass().getName() : "null"), e);
        }
    }

    /**
     * Converts a JSON string to an object of the specified class.
     *
     * @param json  the JSON string to parse (not null)
     * @param clazz the target class
     * @param <T>   the type of the target
     * @return the deserialized object, or {@code null} if json is "null"
     * @throws IllegalArgumentException if {@code json} or {@code clazz} is null
     * @throws IllegalStateException if the JSON cannot be parsed
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        Objects.requireNonNull(json, "JSON string must not be null");
        Objects.requireNonNull(clazz, "Target class must not be null");

        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot deserialize JSON to " + 
                    clazz.getName() + ": " + json, e);
        }
    }

    /**
     * Converts a JSON string to an object using a {@link TypeReference}.
     * Example:
     *
     * <pre>
     * List<Foo> list = ObjectUtils.fromJson(jsonString, new TypeReference<List<Foo>>() {
     * });
     * </pre>
     *
     * @param json    the JSON string to parse (not null)
     * @param typeRef the Jackson {@code TypeReference} describing the target type
     * @param <T>     the type of the target
     * @return the deserialized object
     * @throws IllegalArgumentException if {@code json} or {@code typeRef} is null
     * @throws IllegalStateException if the JSON cannot be parsed
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        Objects.requireNonNull(json, "JSON string must not be null");
        Objects.requireNonNull(typeRef, "Target type reference must not be null");

        try {
            return MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot deserialize JSON to " + 
                    typeRef.getType() + ": " + json, e);
        }
    }

    /**
     * Compares two objects for deep equality by serializing them to JsonNode using
     * Jackson.
     * Handles null values appropriately.
     *
     * @param a the first object to compare
     * @param b the second object to compare
     * @return true if both objects are deeply equal, false otherwise
     */
    public static <T> boolean deepEquals(T a, T b) {

        // If both are the same reference, they are equal
        if (a == b) return true;

        // If one is null and the other is not, they are not equal
        if (a == null || b == null) return false;

        // Serialize both objects to JsonNode and compare them
        try {
            JsonNode na = MAPPER.valueToTree(a);
            JsonNode nb = MAPPER.valueToTree(b);
            return na.equals(nb);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Cannot compare " +
                    a.getClass().getName() + " and " + b.getClass().getName() +
                    ": JSON tree conversion failed.", e);
        }
    }
}

package org.apache.fontbox.ttf.advanced.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.fontbox.ttf.advanced.AdvancedTypographicTableFormatException;
import org.apache.fontbox.ttf.advanced.SubtableEntryHolder.SubtableEntry;

public class AdvancedChecker {
    private AdvancedChecker() { }

    /**
     * Extracts the {@link SubtableEntry} with the given {@code index} from the {@code list} and checks
     * that it is non-null and is of the {@code required} type. If not throws an {@link AdvancedTypographicTableFormatException}.
     * @return the {@link SubtableEntry} cast to the {@code required} type
     */
    @SuppressWarnings("unchecked")
    public static <T extends SubtableEntry> T checkGet(List<SubtableEntry> list, int index, Class<T> required) {
        SubtableEntry o = list.get(index);
        if (o == null || o.getClass() != required) {
            throw new AdvancedTypographicTableFormatException(
                String.format(Locale.ROOT, "illegal entries, entry %d must be an %s, but is: %s",
                    index, required.getSimpleName(), (o != null) ? o.getClass().getSimpleName() : null));
        }
        return (T) o;
    }

    /**
     * Checks that {@code entries} is exactly {@code size} and throws a
     * {@link AdvancedTypographicTableFormatException} if not.
     */
    public static void checkSize(List<SubtableEntry> entries, int size) {
        if (entries == null || entries.size() != size) {
            throw new AdvancedTypographicTableFormatException(
                String.format(Locale.ROOT, "illegal entries, must be non-null and contain exactly %d entries", size));
        }
    }

    /**
     * Checks that {@code gid} is in the range {@code 0..65535} inclusive or throws
     * an {@link AdvancedTypographicTableFormatException} with the {@code exMsg} supplied.
     */
    public static void checkGidRange(int gid, Supplier<String> exMsg) {
        if ((gid < 0) || (gid > 65535)) {
            throw new AdvancedTypographicTableFormatException(exMsg.get());
        }
    }


    /**
     * Checks that {@code gid} complies with the {@code predicate} or throws an
     * {@link AdvancedTypographicTableFormatException} with the {@code exMsg} supplied.
     */
    public static void checkCondition(int gid, IntPredicate predicate, Supplier<String> exMsg) {
        if (!predicate.test(gid)) {
            throw new AdvancedTypographicTableFormatException(exMsg.get());
        }
    }

    /**
     * Returns an {@link IntPredicate} that tests that the subject is not greater than {@code max}.
     */
    public static IntPredicate notGt(int max) {
        return gid -> !(gid > max);
    }

    /**
     * Returns an {@link IntPredicate} that tests that the subject is not less than {@code min}.
     */
    public static IntPredicate notLt(int min) {
        return gid -> !(gid < min);
    }

    /**
     * If {@code entries} is non-null then iterates over it applying {@code transform}
     * to each item and returning the transformed items in a new list.
     * If {@code entries} is null, returns a mutable empty list.
     */
    public static <T, U> List<U> arrayMap(T[] entries, Function<T, U> transform) {
        if (entries != null) {
            List<U> result = new ArrayList<>(entries.length);
            for (int i = 0; i < entries.length; i++) {
                result.add(transform.apply(entries[i]));
            }
            return result;
        }
        return new ArrayList<>();
    }

    /**
     * If {@code entries} is non-null then iterates over it applying {@code transform}
     * to each item and returning the transformed items in a new list.
     * If {@code entries} is null, returns a mutable empty list.
     */
    public static <U> List<U> arrayMap(int[] entries, IntFunction<U> transform) {
        if (entries != null) {
            List<U> result = new ArrayList<>(entries.length);
            for (int i = 0; i < entries.length; i++) {
                result.add(transform.apply(entries[i]));
            }
            return result;
        }
        return new ArrayList<>();
    }

    /**
     * Calls {@code transform} in the range {@code 0..size} exclusive and 
     * add the result to a list which is returned. If {@code size} is zero
     * returns an empty mutable list.
     */
    public static <U> List<U> rangeMap(int size, IntFunction<U> transform) {
        List<U> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(transform.apply(i));
        }
        return result;
    }

    /**
     * Returns true if all {@code entries} can be assigned to {@code type}.
     */
    public static boolean allOfType(List<SubtableEntry> entries, Class<? extends SubtableEntry> type) {
        for (SubtableEntry entry : entries) {
            if (!type.isAssignableFrom(entry.getClass())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a mutable list with the one supplied {@code item}.
     */
    public static <T> List<T> mutableSingleton(T item) {
        List<T> list = new ArrayList<>(1);
        list.add(item);
        return list;
    }

    /**
     * Returns a comma separated string representing the simple class
     * names of all items in {@code objects}.
     */
    public static String toClassString(List<? extends Object> objects, String prefix, String suffix) {
        return objects
          .stream()
          .map(Object::getClass)
          .map(Class::getSimpleName)
          .collect(Collectors.joining(", ", prefix, suffix));
    }

        /**
     * If {@code items} is non-null then transforms every item using {@code transform} and
     * if the result is non-null it is passed to {@code consume}.
     * @param <T> array type
     * @param <U> result type of transform
     * @param items a possibly null list of items
     * @param transform a transform function called for each item in items
     * @param consume a consumer called for each non-null result from transform
     */
    public static <T, U> void transformConsume(T[] items, Function<T, U> transform, Consumer<U> consume) {
        if (items != null) {
            for (T item : items) {
                U transformed = transform.apply(item);
                if (transformed != null) {
                    consume.accept(transformed);
                }
            }
        }
    }

    
    /**
     * If {@code items} is non-null then transforms every item using {@code transform} and
     * if the result is non-null it is passed to {@code consume}.
     * @param <T> list item type
     * @param <U> result type of transform
     * @param items a possibly null list of items
     * @param transform a transform function called for each item in items
     * @param consume a consumer called for each non-null result from transform
     */
    public static <T, U> void transformConsume(List<T> items, Function<T, U> transform, Consumer<U> consume) {
        if (items != null) {
            for (T item : items) {
                U transformed = transform.apply(item);
                if (transformed != null) {
                    consume.accept(transformed);
                }
            }
        }
    }
}

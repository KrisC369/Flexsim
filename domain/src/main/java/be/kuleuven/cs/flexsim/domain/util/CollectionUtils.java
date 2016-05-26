/**
 *
 */
package be.kuleuven.cs.flexsim.domain.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

/**
 * This class represents some generic utility function making use of mapping
 * functions.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public final class CollectionUtils {
    private CollectionUtils() {
    }

    /**
     * This calculates the max of a certain value in a list of objects having
     * comparable values.
     *
     * @param elements
     *            the list of elements.
     * @param f
     *            the function to apply to an element to get the value to
     *            compare with the maximum.
     * @param <T>
     *            the type representing the elements to apply function f to.
     * @return the maximum.
     */
    public static <T> int max(Iterable<T> elements, IntNNFunction<T> f) {
        if (!elements.iterator().hasNext()) {
            throw new NoSuchElementException("The supplied iterable is empty");
        }
        return StreamSupport.stream(elements.spliterator(), false)
                .mapToInt(f::apply).max().getAsInt();
    }

    /**
     * This calculates and returns the element of the elements for which the
     * application of f to that element reaches its maximum.
     *
     * @param elements
     *            the elements of elements.
     * @param f
     *            the function to apply to an element to get the value to
     *            compare with the maximum.
     * @param <T>
     *            the type representing the elements to apply function f to.
     * @return the argument attaining the maximum in f.
     */
    public static <T> T argMax(Iterable<T> elements, IntNNFunction<T> f) {
        checkArgument(elements.iterator().hasNext(),
                "Can't provide empty elements to this function");
        Iterator<T> it = elements.iterator();
        T currentMax = it.next();
        int max = f.apply(currentMax);
        while (it.hasNext()) {
            T current = it.next();
            if (f.apply(current) > max) {
                max = f.apply(current);
                currentMax = current;
            }
        }
        return currentMax;
    }

    /**
     * This calculates the sum of a certain value over a list of objects having
     * that value.
     *
     * @param elements
     *            the list of elements.
     * @param f
     *            the function to apply to an element to get the value to sum.
     * @param <T>
     *            the type representing the elements to apply function f to.
     *            over.
     * @return the sum over all elements.
     */
    public static <T> int sum(Iterable<T> elements, IntNNFunction<T> f) {
        return StreamSupport.stream(elements.spliterator(), false)
                .mapToInt(f::apply).sum();
    }
}

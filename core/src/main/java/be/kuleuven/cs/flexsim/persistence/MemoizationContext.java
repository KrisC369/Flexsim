package be.kuleuven.cs.flexsim.persistence;

import com.google.common.base.Supplier;

/**
 * Memoization context for storing costly calculated results for later use without the calculation.
 * Serves as a sort of cache.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface MemoizationContext<E, R> {

    //TODO consolidate API to only have one memoization method with fucall arg.

    /**
     * Save the results from this entry parameters in the memoization context.
     *
     * @param entry  The entry parameters.
     * @param result The costly calculated results.
     */
    @Deprecated
    void memoizeEntry(E entry, R result);

    /**
     * Test if this context has results stored for this entry.
     *
     * @param entry The entry parameters.
     * @return True if precalculated results are available.
     */
    @Deprecated
    boolean hasResultFor(E entry);

    /**
     * Get the results stored in the memoization context.
     *
     * @param entry The entry parameters.
     * @return The precalculated results.
     */
    @Deprecated
    R getMemoizedResultFor(E entry);

    /**
     * Classic memoiztion function.
     *
     * @param entry
     * @param calculationFu
     * @return
     */
    R testAndCall(E entry, Supplier<R> calculationFu);
}

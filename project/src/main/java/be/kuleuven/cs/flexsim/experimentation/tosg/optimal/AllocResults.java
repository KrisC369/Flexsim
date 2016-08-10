package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.experimentation.tosg.FlexProvider;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class AllocResults {
    /**
     * Represents an infeasible solution.
     */
    public static final AllocResults INFEASIBLE = create(ArrayListMultimap.create(), -1);

    /**
     * @return The allocation results as a map of provider to list of booleans indicating
     * activation moments.
     */
    public abstract ListMultimap<FlexProvider, Boolean> getAllocationResults();

    /**
     * @return The objective function result value.
     */
    public abstract double getObjective();

    /**
     * Factory method.
     *
     * @param allocs    The allocation results.
     * @param objective The objective function value.
     * @return A value object containing the results.
     */
    public static AllocResults create(final ListMultimap<FlexProvider, Boolean> allocs,
            final double objective) {
        return new AutoValue_AllocResults(allocs, objective);
    }

}
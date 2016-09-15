package be.kuleuven.cs.flexsim.domain.energy.dso.r3dp;

import be.kuleuven.cs.flexsim.domain.util.data.AbstractTimeSeriesImplementation;

/**
 * Represents the context for which to solve flexibility allocation problems.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface FlexAllocProblemContext {

    /**
     * @return The flexibility providers present for this problem context.
     */
    Iterable<FlexibilityProvider> getProviders();

    /**
     * @return The energy profile to minimize as a goal for the solver.
     */
    AbstractTimeSeriesImplementation getEnergyProfileToMinimizeWithFlex();

}

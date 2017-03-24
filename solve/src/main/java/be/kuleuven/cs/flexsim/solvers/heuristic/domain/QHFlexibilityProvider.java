package be.kuleuven.cs.flexsim.solvers.heuristic.domain;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.solvers.optimal.ConstraintConversion;
import be.kuleuven.cs.flexsim.solvers.data.QuarterHourlyFlexConstraints;

/**
 * A quarter hourly discretized flexibility provider.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface QHFlexibilityProvider extends FlexibilityProvider {
    /**
     * Automatic conversion from hourly to quarter hourly converstion.
     *
     * @return a quarter hourly activation constraint object.
     */
    default QuarterHourlyFlexConstraints getQHFlexibilityActivationConstraints() {
        return ConstraintConversion.fromHourlyToQuarterHourly(
                getFlexibilityActivationConstraints());
    }

    /**
     * @return The wrapped provider.
     */
    FlexibilityProvider getWrappedProvider();

}

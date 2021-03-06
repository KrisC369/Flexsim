package be.kuleuven.cs.gridflex.solvers.common;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.domain.util.Payment;
import be.kuleuven.cs.gridflex.domain.util.data.DoublePowerCapabilityBand;

/**
 * This flex provider wraps a domain entity provider and converts hourly constraints and data to
 * the current discretization level necessary.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class QHFlexibilityProviderDecorator implements QHFlexibilityProvider {
    private static final double HOUR_TO_QUARTER_HOUR = 0.25d;
    private final FlexibilityProvider provider;

    public QHFlexibilityProviderDecorator(FlexibilityProvider provider) {
        this.provider = provider;
    }

    @Override
    public DoublePowerCapabilityBand getFlexibilityActivationRate() {
        return DoublePowerCapabilityBand
                .create(provider.getFlexibilityActivationRate().getDown() * HOUR_TO_QUARTER_HOUR,
                        provider.getFlexibilityActivationRate().getUp() * HOUR_TO_QUARTER_HOUR);
    }

    @Override
    public HourlyFlexConstraints getFlexibilityActivationConstraints() {
        return provider.getFlexibilityActivationConstraints();
    }

    @Override
    public double getMonetaryCompensationValue() {
        return provider.getMonetaryCompensationValue();
    }

    @Override
    public void registerActivation(FlexActivation activation, Payment payment) {
        provider.registerActivation(activation, payment);
    }

    @Override
    public FlexibilityProvider getWrappedProvider() {
        return provider;
    }

    @Override
    public String toString() {
        return "QHFlexibilityProviderDecorator{" +
                "rateUp=" + provider.getFlexibilityActivationRate().getUp() +
                '}';
    }
}

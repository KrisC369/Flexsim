package be.kuleuven.cs.flexsim.domain.energy.dso.r3dp;

import be.kuleuven.cs.flexsim.domain.util.Payment;
import be.kuleuven.cs.flexsim.domain.util.data.DoublePowerCapabilityBand;
import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A provider of flexibility with activation constraints.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class FlexProvider implements FlexibilityProvider {
    private static final double FIXED_PRICE = 35.4;
    private final HourlyFlexConstraints constraints;
    private final double powerRate;
    private final List<FlexActivation> activations;
    private long runningCompensationValue;

    /**
     * Constructor
     *
     * @param powerRate The powerrate this provider can offer.
     */
    public FlexProvider(final double powerRate) {
        this(powerRate, HourlyFlexConstraints.R3DP);
    }

    /**
     * Constructor with contract.
     *
     * @param powerRate The powerrate this provider can offer.
     * @param contract  The contract containing the flexibility constraints agreed upon.
     */
    public FlexProvider(final double powerRate, final HourlyFlexConstraints contract) {
        this.constraints = contract;
        this.powerRate = powerRate;
        this.activations = Lists.newArrayList();
    }

    @Override
    public DoublePowerCapabilityBand getFlexibilityActivationRate() {
        return DoublePowerCapabilityBand.create(0, powerRate);
    }

    @Override
    public HourlyFlexConstraints getFlexibilityActivationConstraints() {
        return constraints;
    }

    @Override
    public long getMonetaryCompensationValue() {
        return runningCompensationValue;
    }

    @Override
    public void registerActivation(FlexActivation activation, Payment payment) {
        checkForActivationConstraintViolation(activation);
        addActivation(activation);
        //registerCompensation(activation); //TODO one or the other...
        registerCompensation(payment);
    }

    private void addActivation(FlexActivation activation) {
        this.activations.add(activation);
    }

    @Deprecated
    private void registerCompensation(FlexActivation activation) {
        runningCompensationValue += activation.getEnergyVolume() * FIXED_PRICE;
    }

    private void registerCompensation(Payment compensation) {
        runningCompensationValue += compensation.getMonetaryAmount();
    }

    private void checkForActivationConstraintViolation(FlexActivation activation) {
        checkArgument(activation.getDuration() == constraints.getActivationDuration(),
                "Activation duration does not match constraints. Got: " + activation.getDuration());
        if (hasActivations()) {
            double timeBetweenLast =
                    (activation.getStart() - (getLastActivation().getStart() + getLastActivation()
                            .getDuration()));
            checkArgument(
                    timeBetweenLast >= constraints.getInterActivationTime(),
                    "Time between activations should be at least " + constraints
                            .getInterActivationTime() + " hours, but was: " + timeBetweenLast);
        }
        checkArgument(
                activation.getEnergyVolume() <= powerRate * constraints.getActivationDuration());
    }

    private boolean hasActivations() {
        return activations.size() > 0;
    }

    FlexActivation getLastActivation() {
        return activations.get(activations.size() - 1);
    }
}

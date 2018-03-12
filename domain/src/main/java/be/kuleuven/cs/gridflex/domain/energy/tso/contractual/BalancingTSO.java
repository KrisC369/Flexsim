package be.kuleuven.cs.gridflex.domain.energy.tso.contractual;

import be.kuleuven.cs.gridflex.domain.energy.consumption.EnergyConsumptionTrackable;
import be.kuleuven.cs.gridflex.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.gridflex.domain.energy.tso.MechanismHost;
import be.kuleuven.cs.gridflex.domain.energy.tso.simple.CopperplateTSO;
import be.kuleuven.cs.gridflex.util.CollectionUtils;
import be.kuleuven.cs.gridflex.domain.util.data.IntPowerCapabilityBand;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.eclipse.jdt.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A TSO implementation that can accept bids for balancing actions and clears
 * the bids, optimally selecting the best choices.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class BalancingTSO extends CopperplateTSO
        implements MechanismHost<ContractualMechanismParticipant> {
    private final LinkedHashSet<ContractualMechanismParticipant> participants;
    private final Map<ContractualMechanismParticipant, @NonNull IntPowerCapabilityBand> powerLimits;

    /**
     * Constructor with consumption instances as parameter.
     *
     * @param sites The consumption sites connected to this TSO
     */
    public BalancingTSO(final EnergyConsumptionTrackable... sites) {
        this(new EnergyProductionTrackable[0], sites);
    }

    /**
     * Constructor with production instances as parameter.
     *
     * @param sites The production sites connected to this TSO
     */
    public BalancingTSO(final EnergyProductionTrackable... sites) {
        this(sites, new EnergyConsumptionTrackable[0]);
    }

    /**
     * Constructor with no initial partakers.
     */
    public BalancingTSO() {
        this(new EnergyProductionTrackable[0],
                new EnergyConsumptionTrackable[0]);
    }

    /**
     * Actual initializing constructor.
     *
     * @param prod the producers.
     * @param cons the consumers.
     */
    private BalancingTSO(final EnergyProductionTrackable[] prod,
            final EnergyConsumptionTrackable[] cons) {
        super(prod, cons);
        this.participants = Sets.newLinkedHashSet();
        this.powerLimits = Maps.newLinkedHashMap();
    }

    /**
     * @return the participants
     */
    public List<ContractualMechanismParticipant> getParticipants() {
        return Collections.unmodifiableList(Lists.newArrayList(participants));
    }

    @Override
    public void afterTick(final int t) {
        super.afterTick(t);
        pollCapacities();
        calculateAndSignal(t);
    }

    private void pollCapacities() {
        for (final ContractualMechanismParticipant p : participants) {
            testValidParticipant(p);
            this.powerLimits.put(p, p.getPowerCapacity());
        }
    }

    private void calculateAndSignal(final int timestep) {
        if (getCurrentImbalance() > 0) {
            final int sum = CollectionUtils.sum(
                    Lists.newArrayList(powerLimits.values()),
                    IntPowerCapabilityBand::getUp);

            if (sum <= Math.abs(getCurrentImbalance())) {
                sendSignal(timestep, 1, true);
            } else {
                sendSignal(timestep, getFactor(sum, getCurrentImbalance()),
                        true);
            }
        } else if (getCurrentImbalance() < 0) {
            final int sum = CollectionUtils.sum(
                    Lists.newArrayList(powerLimits.values()),
                    IntPowerCapabilityBand::getDown);
            if (sum <= Math.abs(getCurrentImbalance())) {
                sendSignal(timestep, 1, false);
            } else {
                sendSignal(timestep, getFactor(sum, getCurrentImbalance()),
                        false);
            }
        } else {
            sendSignal(timestep, 0, true);
        }
    }

    @SuppressWarnings("null")
    private void sendSignal(final int t, final double frac, final boolean upflex) {

        for (final Entry<ContractualMechanismParticipant, IntPowerCapabilityBand> e :
                powerLimits
                        .entrySet()) {
            int value;
            if (upflex) {
                value = e.getValue().getUp();
            } else {
                value = e.getValue().getDown() * -1;
            }
            e.getKey().signalTarget(t, (int) Math.round(value * frac));
        }

    }

    private static double getFactor(final double sum, final double currentImbalance) {
        if (sum == 0 || currentImbalance == 0) {
            return 0;
        }
        return Math.abs(currentImbalance) / sum;
    }

    @Override
    public void registerParticipant(
            final ContractualMechanismParticipant participant) {
        this.participants.add(participant);
        this.powerLimits.put(participant, IntPowerCapabilityBand.createZero());

    }

    /**
     * Returns the contractual limits registered to a participant.
     *
     * @param agg The client to check.
     * @return The limits.
     */
    public IntPowerCapabilityBand getContractualLimit(
            final ContractualMechanismParticipant agg) {
        testValidParticipant(agg);
        return checkNotNull(this.powerLimits.get(agg));
    }

    private void testValidParticipant(final ContractualMechanismParticipant agg) {
        if (!hasParticipant(agg)) {
            throw new IllegalStateException(
                    "Should have this aggregator registered before calling this method.");
        }
    }

    private boolean hasParticipant(final ContractualMechanismParticipant agg) {
        return this.participants.contains(agg);
    }

    /**
     * Signal that this participant has a new margin of power capabilities.
     *
     * @param agg The client
     * @param cap The new capabilities.
     */
    public void signalNewLimits(final ContractualMechanismParticipant agg,
            final IntPowerCapabilityBand cap) {
        testValidParticipant(agg);
        this.powerLimits.put(agg, cap);
    }
}

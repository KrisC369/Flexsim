package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.site.ActivateFlexCommand;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.tso.SteeringSignal;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Represents an energy aggregator implementation.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class AggregatorImpl implements SimulationComponent {
    private final Set<SiteFlexAPI> clients;
    private final SteeringSignal tso;
    private int tickcount;
    private final int aggFreq;

    /**
     * Default constructor.
     *
     * @param tso
     *            the tso to take steering signals from.
     * @param frequency
     *            the frequency with which to perform aggregation functions.
     */
    public AggregatorImpl(SteeringSignal tso, int frequency) {
        this.clients = Sets.newLinkedHashSet();
        this.tso = tso;
        this.tickcount = 1;
        this.aggFreq = frequency;
    }

    /**
     * @return the clients.
     */
    final List<SiteFlexAPI> getClients() {
        return Lists.newArrayList(clients);
    }

    /**
     * @return the tso.
     */
    final SteeringSignal getTso() {
        return tso;
    }

    /**
     * Register a client to this aggregator.
     *
     * @param client
     *            the client should expose the siteflex api service.
     */
    public void registerClient(SiteFlexAPI client) {
        clients.add(client);
    }

    private void doAggregationStep(int t) {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> flex = gatherFlexInfo();

        final int target = getTargetFlex(t);
        logStep(t, target);

        AggregationContext dispatch = new AggregationContext() {

            @Override
            public void dispatchActivation(
                    LinkedListMultimap<SiteFlexAPI, FlexTuple> flex,
                    Set<Long> ids) {
                for (SiteFlexAPI s : flex.keySet()) {
                    for (long i : ids) {
                        for (FlexTuple t : flex.get(s)) {
                            if (t.getId() == i) {
                                final FlexTuple tt = t;
                                if (tt.getDirection()) {
                                    logRestore(tt);
                                } else {
                                    logCurtail(tt);
                                }
                                s.activateFlex(new ActivateFlexCommand() {
                                    @Override
                                    public long getReferenceID() {
                                        return tt.getId();
                                    }

                                    @Override
                                    public boolean isDownFlexCommand() {
                                        return !tt.getDirection();
                                    }
                                });
                            }
                        }
                    }
                }
            }
        };

        AggregationStrategyImpl.CARTESIANPRODUCT.performAggregationStep(dispatch, t, flex,
                target);

    }

    private int getTargetFlex(int t) {
        return getTso().getCurrentValue(t) * 1;
    }

    private LinkedListMultimap<SiteFlexAPI, FlexTuple> gatherFlexInfo() {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> res = LinkedListMultimap
                .create();
        for (SiteFlexAPI s : this.clients) {
            res.putAll(s, s.getFlexTuples());
        }
        return res;
    }

    @Override
    public void initialize(SimulationContext context) {
    }

    @Override
    public void afterTick(int t) {
    }

    @Override
    public void tick(int t) {
        if (tickcount++ % aggFreq == 0) {
            doAggregationStep(t);
        }
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    private void logStep(int t, int target) {
        LoggerFactory
                .getLogger(AggregatorImpl.class)
                .debug("Performing aggregation step at time step {} with flextarget {}",
                        t, target);
    }

    private void logCurtail(FlexTuple tt) {
        LoggerFactory.getLogger(AggregatorImpl.class).debug(
                "Sending curtail request based on profile {}", tt);
    }

    private void logRestore(FlexTuple tt) {
        LoggerFactory.getLogger(AggregatorImpl.class).debug(
                "Sending restore request based on profile {}", tt);
    }
}

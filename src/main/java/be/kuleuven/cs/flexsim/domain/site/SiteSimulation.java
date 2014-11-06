package be.kuleuven.cs.flexsim.domain.site;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.process.FlexProcess;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.flexsim.simulation.UIDGenerator;

import com.google.common.collect.Lists;

/**
 * Class representing a site module that makes abstraction of the underlying
 * mechanism and produces flex and consumption patterns.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class SiteSimulation implements Site {

    private final int maxLimitConsumption;
    private final int minLimitConsumption;
    private final int maxTuples;
    private int currentConsumption;
    private List<FlexTuple> flexData;
    private UIDGenerator generator;
    private int totalConsumption;
    private int baseProduction;

    /**
     * Default constructor for this mock simulating site.
     * 
     * @param base
     *            The base consumption to start from.
     * @param min
     *            The minimum limit for consumption.
     * @param max
     *            The maximum limit for consumption.
     * @param maxTuples
     *            The maximum tuples to generate per section of flex.
     */
    public SiteSimulation(int base, int min, int max, int maxTuples) {
        checkArgument(min <= base && base <= max);
        this.maxLimitConsumption = max;
        this.minLimitConsumption = min;
        this.maxTuples = maxTuples;
        this.currentConsumption = base;
        this.flexData = Lists.newArrayList();
        this.baseProduction = 20;
        this.generator = new UIDGenerator() {
            @Override
            public long getNextUID() {
                return 0;
            }
        };
    }

    @Override
    public List<FlexTuple> getFlexTuples() {
        calculateCurrentFlex();
        return Lists.newArrayList(flexData);
    }

    @Override
    public void activateFlex(ActivateFlexCommand schedule) {
        for (FlexTuple f : flexData) {
            if (f.getId() == schedule.getReferenceID()) {
                if (schedule.isDownFlexCommand()) {
                    currentConsumption -= f.getDeltaP();
                } else {
                    currentConsumption += f.getDeltaP();

                }
            }
        }
    }

    @Override
    public List<Integer> getBufferOccupancyLevels() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Resource> takeResources() {
        List<Resource> res = Lists.newArrayList();
        double factor = (double) (getCurrentConsumption() - getMinLimitConsumption())
                / (double) (getMaxLimitConsumption() - getMinLimitConsumption());

        for (int i = 0; i < Math.ceil(factor) * baseProduction; i++) {
            res.add(ResourceFactory.createResource(0));
        }
        return res;
    }

    @Override
    public void deliverResources(List<Resource> res) {
        throw new UnsupportedOperationException(
                "This implementation does not support resource handling stuff.");
    }

    @Override
    public double getLastStepConsumption() {
        return getCurrentConsumption();
    }

    @Override
    public double getTotalConsumption() {
        return totalConsumption;
    }

    @Override
    public double getAverageConsumption() {
        return getCurrentConsumption();
    }

    @Override
    public void afterTick(int t) {
        updateConsumption();
    }

    private void updateConsumption() {
        this.totalConsumption += getCurrentConsumption();
    }

    private void calculateCurrentFlex() {
        List<FlexTuple> upFlex = Lists.newArrayList();
        List<FlexTuple> downFlex = Lists.newArrayList();
        // upFlex:
        int upDiff = getMaxLimitConsumption() - getCurrentConsumption();
        int diffStep = upDiff / getMaxTuples();
        for (int i = 1; i <= getMaxTuples(); i++) {
            upFlex.add(makeTuple(i * diffStep, true));
        }
        int downDiff = getCurrentConsumption() - getMinLimitConsumption();
        diffStep = downDiff / getMaxTuples();
        for (int i = 1; i <= getMaxTuples(); i++) {
            downFlex.add(makeTuple(i * diffStep, false));
        }
        resetFlex(upFlex, downFlex);
    }

    private FlexTuple makeTuple(int power, boolean isUpflex) {
        return FlexTuple.create(newId(), power, isUpflex, 0, 0, 0);
    }

    private long newId() {
        return generator.getNextUID();
    }

    private void resetFlex(List<FlexTuple> upFlex, List<FlexTuple> downFlex) {
        this.flexData = Lists.newArrayList(upFlex);
        this.flexData.addAll(downFlex);
    }

    @Override
    public void tick(int t) {
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    @Override
    public void initialize(SimulationContext context) {
        this.generator = context.getUIDGenerator();
    }

    @Override
    public boolean containsLine(FlexProcess process) {
        return false;
    }

    /**
     * @return the currentConsumption
     */
    private int getCurrentConsumption() {
        return currentConsumption;
    }

    /**
     * @return the maxLimitConsumption
     */
    private int getMaxLimitConsumption() {
        return maxLimitConsumption;
    }

    /**
     * @return the minLimitConsumption
     */
    private int getMinLimitConsumption() {
        return minLimitConsumption;
    }

    /**
     * @return the maxTuples
     */
    private int getMaxTuples() {
        return maxTuples;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SiteSimulation [#T=").append(maxTuples)
                .append(", cCons=").append(currentConsumption).append("]");
        return builder.toString();
    }

}

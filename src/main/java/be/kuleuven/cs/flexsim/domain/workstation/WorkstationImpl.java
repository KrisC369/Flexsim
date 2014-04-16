package be.kuleuven.cs.flexsim.domain.workstation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.domain.util.NonNullableFunction;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

import com.google.common.annotations.VisibleForTesting;

/**
 * Main workstation class representing machines that perform work and consume
 * energy.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class WorkstationImpl implements Workstation, WorkstationContext {

    private static final NonNullableFunction<Resource, Integer> CURRENT_REMAINING_STEPS = new NonNullableFunction<Resource, Integer>() {
        @Override
        public Integer apply(Resource input) {
            // TODO Auto-generated method stub
            return input.getCurrentNeededProcessTime();
        }
    };

    private static final NonNullableFunction<Resource, Integer> MAX_REMAINING_STEPS = new NonNullableFunction<Resource, Integer>() {
        @Override
        public Integer apply(Resource input) {
            // TODO Auto-generated method stub
            return input.getMaxNeededProcessTime();
        }
    };

    private final Buffer<Resource> inputBuff;
    private final Buffer<Resource> outputBuff;

    private final StationState resourceMovingState;
    private final StationState processingState;
    private StationState currentState;
    private List<Resource> currentResource;
    private double totalConsumption;
    private double lastConsumption;
    private int processedCount;
    private final int fixedECons;
    private final int capacity;

    /**
     * Constructor that creates a workstation instance from an in and an out
     * buffer.
     * 
     * @param bufferIn
     *            The In buffer.
     * @param bufferOut
     *            The Out buffer.
     * @param linear
     */
    @VisibleForTesting
    WorkstationImpl(Buffer<Resource> bufferIn, Buffer<Resource> bufferOut,
            int idle, int working, int capacity, ConsumptionModel model) {
        this.inputBuff = bufferIn;
        this.outputBuff = bufferOut;
        this.fixedECons = idle;
        this.processingState = new StationStateImpl.Processing(working - idle,
                model);
        this.resourceMovingState = new StationStateImpl.ResourceMoving(0, model);
        this.currentState = resourceMovingState;
        this.totalConsumption = 0;
        this.processedCount = 0;
        this.lastConsumption = 0;
        this.capacity = capacity;
        this.currentResource = new ArrayList<>();
    }

    @Override
    public void afterTick() {
    }

    @Override
    public double getLastStepConsumption() {
        return lastConsumption;
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.workstation.IWorkstation#getProcessedItemsCount()
     */
    @Override
    public int getProcessedItemsCount() {
        return this.processedCount;
    }

    @Override
    public double getTotalConsumption() {
        return this.totalConsumption;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * simulation.ISimulationComponent#initialize(simulation.ISimulationContext)
     */
    @Override
    public void initialize(SimulationContext context) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.workstation.IWorkstation#isIdle()
     */
    @Override
    public boolean isIdle() {
        return !currentState.isProcessing();
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.IStationContext#pushConveyer()
     */
    @Override
    public boolean pushConveyer() {
        if (!getCurrentResources().isEmpty()) {
            pushOut();
        }
        if (!getInputBuffer().isEmpty()) {
            pullIn();
            return true;
        }
        setLastConsumption(0);
        return false;
    }

    @VisibleForTesting
    List<Resource> getCurrentResources() {
        return new ArrayList<>(this.currentResource);
    }

    private void pullIn() {
        if (getInputBuffer().getCurrentOccupancyLevel() < getCapacity()) {
            this.addAllResources(getInputBuffer().pullAll());
        } else {
            for (int i = 0; i < getCapacity(); i++) {
                this.addResource(getInputBuffer().pull());
            }
        }
    }

    private void addAllResources(Collection<Resource> res) {
        this.currentResource.addAll(res);
    }

    private void addResource(Resource res) {
        this.currentResource.add(res);
    }

    /**
     * 
     */
    private void pushOut() {
        int size = getCurrentResources().size();
        getOutputBuffer().pushAll(getCurrentResources());
        resetCurrentResource();
        incrementProcessedCount(size);
    }

    @Override
    public void setProcessingState() {
        this.currentState = processingState;

    }

    @Override
    public void setResourceMovingState() {
        this.currentState = resourceMovingState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see simulation.ISimulationComponent#tick()
     */
    @Override
    public void tick() {
        setLastConsumption(getFixedConsumptionRate()
                + getCurrentState().getVarConsumptionRate(getRemainingSteps(),
                        getRemainingMaxSteps()));
        increaseTotalConsumption(getLastStepConsumption());
        currentState.handleTick(this);
    }

    private int getRemainingSteps() {
        return max(currentResource, CURRENT_REMAINING_STEPS);
    }

    private int getRemainingMaxSteps() {
        return max(currentResource, MAX_REMAINING_STEPS);
    }

    private <T> int max(List<T> list, NonNullableFunction<T, Integer> f) {
        int max = 0;
        for (T t : list) {
            if (f.apply(t) > max) {
                max = f.apply(t);
            }
        }
        return max;
    }

    private StationState getCurrentState() {
        return this.currentState;
    }

    private Buffer<Resource> getInputBuffer() {
        return inputBuff;
    }

    private Buffer<Resource> getOutputBuffer() {
        return outputBuff;
    }

    private void increaseTotalConsumption(double consumptionRate) {
        this.totalConsumption = this.totalConsumption + consumptionRate;
    }

    private void incrementProcessedCount(int size) {
        this.processedCount = this.processedCount + size;

    }

    private void resetCurrentResource() {
        this.currentResource = new ArrayList<>();
    }

    private void setLastConsumption(double rate) {
        this.lastConsumption = rate;
    }

    @Override
    public int getFixedConsumptionRate() {
        return fixedECons;
    }

    /**
     * Returns the current capacity of this workstation.
     * 
     * @return the capacity
     */
    public final int getCapacity() {
        return capacity;
    }

    @Override
    public void processResources(int steps) {
        for (Resource r : currentResource) {
            r.process(steps);
        }
    }

    @Override
    public boolean hasUnfinishedResources() {
        for (Resource r : currentResource) {
            if (r.needsMoreProcessing()) {
                return true;
            }
        }
        return false;
    }
}

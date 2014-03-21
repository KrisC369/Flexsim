package domain.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import simulation.SimulationComponent;
import simulation.SimulationContext;
import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.base.Optional;

import domain.resource.Resource;
import domain.util.Buffer;
import domain.workstation.Workstation;
import domain.workstation.WorkstationImpl;

/**
 * A productionline representing buffers and workstations.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class ProductionLine implements SimulationComponent {

    private static final int WORKING_CONSUMPTION = 3;
    private static final int IDLE_CONSUMPTION = 1;

    private final List<Buffer<Resource>> buffers;

    private final List<Workstation> workstations;

    private Optional<SimulationContext> context;

    private ProductionLine() {
        this.buffers = new ArrayList<>();
        this.workstations = new ArrayList<>();
        this.context = Optional.absent();
    }

    @Override
    public void afterTick() {
        long totalLaststep = 0;
        long totalTotal = 0;
        for (Workstation w : workstations) {
            totalLaststep += w.getLastStepConsumption();
            totalTotal += w.getTotalConsumption();
        }
        notifyConsumption(totalLaststep, totalTotal);
    }

    /**
     * Deliver all the resources and use it as input for the line.
     * 
     * @param res
     *            the resources to use.
     */
    public void deliverResources(List<Resource> res) {
        buffers.get(0).pushAll(res);
    }

    /**
     * Returns the number of workstations in this line.
     * 
     * @return The number of workstations in this line.
     */
    public int getNumberOfWorkstations() {
        return this.workstations.size();
    }

    @Override
    public void initialize(SimulationContext context) {
        for (Workstation w : workstations) {
            context.register(w);
        }
        this.context = Optional.of(context);
    }

    /**
     * Take all the processed resources from the end of the line.
     * 
     * @return the processed resources.
     */
    public Collection<Resource> takeResources() {
        return buffers.get(buffers.size() - 1).pullAll();

    }

    @Override
    public void tick() {
    }

    private void notifyConsumption(Long totalLaststep, Long totalTotal) {
        if (this.context.isPresent()) {
            Event e = getContext().getEventFactory().build("report");
            e.setAttribute("time", getContext().getSimulationClock()
                    .getTimeCount());
            e.setAttribute("totalLaststepE", totalLaststep);
            e.setAttribute("totalTotalE", totalTotal);
            getContext().getEventbus().post(e);
        }
    }

    private SimulationContext getContext() {
        return this.context.get();
    }

    /**
     * Creates a productionline with a more complex layout.
     * <code>O-XXX-O-X-O</code> with O as buffers and X as stations and
     * <code>XXX</code> as parallel stations.
     * 
     * @return A productionline instance.
     */
    public static ProductionLine createExtendedLayout() {
        ProductionLine line = new ProductionLine();
        Buffer<Resource> bIn = new Buffer<>();
        Buffer<Resource> b2 = new Buffer<>();
        Buffer<Resource> bOut = new Buffer<>();
        line.buffers.add(bIn);
        line.buffers.add(b2);
        line.buffers.add(bOut);
        line.workstations.add(WorkstationImpl.createConsuming(bIn, b2,
                IDLE_CONSUMPTION, WORKING_CONSUMPTION));
        line.workstations.add(WorkstationImpl.createConsuming(bIn, b2,
                IDLE_CONSUMPTION, WORKING_CONSUMPTION));
        line.workstations.add(WorkstationImpl.createConsuming(bIn, b2,
                IDLE_CONSUMPTION, WORKING_CONSUMPTION));
        line.workstations.add(WorkstationImpl.createConsuming(b2, bOut,
                IDLE_CONSUMPTION, WORKING_CONSUMPTION));
        return line;
    }

    /**
     * Creates a productionline with a simple layout. O-X-O with O as buffers
     * and X as stations.
     * 
     * @return A productionline instance.
     */
    public static ProductionLine createSimpleLayout() {
        ProductionLine line = new ProductionLine();
        Buffer<Resource> bIn = new Buffer<>();
        Buffer<Resource> bOut = new Buffer<>();
        line.buffers.add(bIn);
        line.workstations.add(WorkstationImpl.createConsuming(bIn, bOut,
                IDLE_CONSUMPTION, WORKING_CONSUMPTION));
        line.buffers.add(bOut);
        return line;
    }
}

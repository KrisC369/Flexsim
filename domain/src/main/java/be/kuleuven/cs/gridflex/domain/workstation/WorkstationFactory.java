package be.kuleuven.cs.gridflex.domain.workstation;

import be.kuleuven.cs.gridflex.domain.resource.Resource;
import be.kuleuven.cs.gridflex.domain.util.Buffer;

/**
 * Helper class for creating and instantiating workstation objects.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public final class WorkstationFactory {

    private WorkstationFactory() {
    }

    /**
     * Factory method for workstations that consume energy.
     * 
     * @param in
     *            The inputbuffer instance.
     * @param out
     *            The outputbuffer instance.
     * @param idle
     *            The energy consumption in idle state.
     * @param working
     *            The energy consumption in working state.
     * @return A Ready to use Workstation object.
     */
    public static Workstation createConsuming(final Buffer<Resource> in,
            final Buffer<Resource> out, final int idle, final int working) {
        return new WorkstationImpl(in, out, idle, working, 1,
                ConsumptionModel.CONSTANT);
    }

    /**
     * Factory method for default workstations without energy consumption.
     * 
     * @param bufferIn
     *            The inputbuffer instance.
     * @param bufferOut
     *            The outputbuffer instance.
     * @return A Ready to use Workstation object.
     */
    public static Workstation createDefault(final Buffer<Resource> bufferIn,
            final Buffer<Resource> bufferOut) {
        return new WorkstationImpl(bufferIn, bufferOut, 0, 0, 1,
                ConsumptionModel.CONSTANT);
    }

    /**
     * Factory method for workstations that consume energy and starts execution
     * shifted in time by specified amount of timesteps.
     * 
     * @param in
     *            The inputbuffer instance.
     * @param out
     *            The outputbuffer instance.
     * @param idle
     *            The energy consumption in idle state.
     * @param working
     *            The energy consumption in working state.
     * @param shift
     *            The amount of timesteps to delay the start of execution.
     * @return A Ready to use Workstation object.
     */
    public static Workstation createShiftableWorkstation(final Buffer<Resource> in,
            final Buffer<Resource> out, final int idle, final int working, final int shift) {
        return new DelayedStartStationDecorator(shift, new WorkstationImpl(in,
                out, idle, working, 1, ConsumptionModel.CONSTANT));
    }

    /**
     * Factory method for workstations that consume energy and allow curtailment
     * of all functionality.
     * 
     * @param in
     *            The inputbuffer instance.
     * @param out
     *            The outputbuffer instance.
     * @param idle
     *            The energy consumption in idle state.
     * @param working
     *            The energy consumption in working state.
     * @param shift
     *            The amount of timesteps to delay the start of execution.
     * @return A Ready to use Workstation object.
     */
    public static Workstation createCurtailableStation(final Buffer<Resource> in,
            final Buffer<Resource> out, final int idle, final int working, final int shift) {
        return new CurtailableStationDecorator<Workstation>(
                new DelayedStartStationDecorator(shift, new WorkstationImpl(in,
                        out, idle, working, 1, ConsumptionModel.CONSTANT)));
    }

    /**
     * Factory method for workstations that consume energy and can handle
     * multiple items at once.
     * 
     * @param in
     *            The inputbuffer instance.
     * @param out
     *            The outputbuffer instance.
     * @param idle
     *            The energy consumption in idle state.
     * @param working
     *            The energy consumption in working state.
     * @param capacity
     *            The capacity of this workstation in terms of resources.
     * @return A Ready to use Workstation object.
     */
    public static Workstation createMultiCapConsuming(final Buffer<Resource> in,
            final Buffer<Resource> out, final int idle, final int working, final int capacity) {
        return new SteerableCurtailableStationDecorator(new WorkstationImpl(in,
                out, idle, working, capacity, ConsumptionModel.CONSTANT));

    }

    /**
     * Factory method for workstations that consume energy using a linear
     * consumption model.
     * 
     * @param in
     *            The inputbuffer instance.
     * @param out
     *            The outputbuffer instance.
     * @param idle
     *            The energy consumption in idle state.
     * @param working
     *            The energy consumption in working state.
     * @param capacity
     *            The capacity of this workstation in terms of resources.
     * @return A Ready to use Workstation object.
     */
    public static Workstation createMultiCapLinearConsuming(final Buffer<Resource> in,
            final Buffer<Resource> out, final int idle, final int working, final int capacity) {
        return new SteerableCurtailableStationDecorator(new WorkstationImpl(in,
                out, idle, working, capacity, ConsumptionModel.LINEAR));

    }

    /**
     * Factory method for workstations that consume energy using an exponential
     * consumption model.
     * 
     * @param in
     *            The inputbuffer instance.
     * @param out
     *            The outputbuffer instance.
     * @param idle
     *            The energy consumption in idle state.
     * @param working
     *            The energy consumption in working state.
     * @param capacity
     *            The capacity of this workstation in terms of resources.
     * @return A Ready to use Workstation object.
     */

    public static Workstation createMultiCapExponentialConsuming(
            final Buffer<Resource> in, final Buffer<Resource> out, final int idle, final int working,
            final int capacity) {
        return new SteerableCurtailableStationDecorator(new WorkstationImpl(in,
                out, idle, working, capacity, ConsumptionModel.EXPONENTIAL));
    }

    /**
     * Factory method for workstations that consume energy in two modes with a
     * random noise factor.
     * 
     * @param in
     *            The inputbuffer instance.
     * @param out
     *            The outputbuffer instance.
     * @param idle
     *            The energy consumption in low consumption state.
     * @param working
     *            The energy consumption in high consumption state.
     * @param capacity
     *            The capacity of this workstation in terms of resources.
     * @param width
     *            The width of the noise band.
     * @return A Ready to use Workstation object.
     */
    public static Workstation createRFDualModeStation(final Buffer<Resource> in,
            final Buffer<Resource> out, final int idle, final int working, final int width,
            final int capacity) {
        return new RFSteerableStationDecorator(new WorkstationImpl(in, out, 0,
                idle, capacity, ConsumptionModel.CONSTANT), working, idle,
                width);
    }
}

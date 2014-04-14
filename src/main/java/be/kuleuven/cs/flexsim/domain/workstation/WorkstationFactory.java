package be.kuleuven.cs.flexsim.domain.workstation;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;

/**
 * Helper class for creating and instantiating workstation objects.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
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
     * @return A Ready to use IWorkstation object.
     */
    public static Workstation createConsuming(Buffer<Resource> in,
            Buffer<Resource> out, int idle, int working) {
        return new WorkstationImpl(in, out, idle, working, 1);
    }

    /**
     * Factory method for default workstations without energy consumption.
     * 
     * @param bufferIn
     *            The inputbuffer instance.
     * @param bufferOut
     *            The outputbuffer instance.
     * @return A Ready to use IWorkstation object.
     */
    public static Workstation createDefault(Buffer<Resource> bufferIn,
            Buffer<Resource> bufferOut) {
        return new WorkstationImpl(bufferIn, bufferOut, 0, 0, 1);
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
     * @return A Ready to use IWorkstation object.
     */
    public static Workstation createShiftableWorkstation(Buffer<Resource> in,
            Buffer<Resource> out, int idle, int working, int shift) {
        return new DelayedStartStationDecorator(shift, new WorkstationImpl(in,
                out, idle, working, 1));
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
     * @return A Ready to use IWorkstation object.
     */
    public static Workstation createCurtailableStation(Buffer<Resource> in,
            Buffer<Resource> out, int idle, int working, int shift) {
        return new CurtailableStationDecorator(
                new DelayedStartStationDecorator(shift, new WorkstationImpl(in,
                        out, idle, working, 1)));
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
     * 
     * @param capacity
     *            The capacity of this workstation in terms of resources.
     * @return A Ready to use IWorkstation object.
     */
    public static Workstation createMultiCapConsuming(Buffer<Resource> in,
            Buffer<Resource> out, int idle, int working, int capacity) {
        return new WorkstationImpl(in, out, idle, working, capacity);
    }
}

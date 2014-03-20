package simulation;

/**
 * The interface for the simulation context.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface ISimulationContext {

    /**
     * This method registers a simulationcomponent to the simulation context in
     * order to receive ticks in every simulated timestep. After registration, a
     * callback is initiated for dependency injection of this context.
     * 
     * Registration also encorporates registering the component to the
     * <code>EventBus<code>
     * 
     * @param comp
     *            the component to register.
     */
    void register(ISimulationComponent comp);

}

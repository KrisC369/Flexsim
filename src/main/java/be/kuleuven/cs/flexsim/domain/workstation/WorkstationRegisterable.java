package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * Represents an instance workstations should be able to register themselves to
 * for whatever reason. This interface provides a means to register the specific
 * subtype. (Similarities with visitors)
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface WorkstationRegisterable {

    /**
     * Register a regular workstation.
     * 
     * @param workstation
     *            the instance.
     */
    void register(Workstation workstation);

    /**
     * Register a curtailable workstation.
     * 
     * @param workstation
     *            the instance.
     */
    void register(CurtailableWorkstation workstation);

    /**
     * Register a steerable workstation instance.
     * 
     * @param ws
     *            the instance.
     */
    void register(TradeofSteerableWorkstation ws);

    /**
     * Register a dual mode workstation instance.
     * 
     * @param ws
     *            the instance.
     */
    void register(DualModeWorkstation ws);

}

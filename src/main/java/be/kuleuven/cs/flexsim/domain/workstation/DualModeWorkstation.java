package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * This workstation instance is is capable of switching between two consumption
 * modes.
 */
public interface DualModeWorkstation extends Workstation {

    /**
     * Switch high consumption mode on.
     */
    void signalHighConsumption();

    /**
     * Switch low consumption mode on.
     */
    void signalLowConsumption();

    /**
     * Returns the Energy consumption rate when in high consumption.
     * 
     * @return the consumption rate.
     */
    int highConsumptionRate();

    /**
     * Returns the Energy consumption rate when in low consumption.
     * 
     * @return the consumption rate.
     */
    int lowConsumptionRate();

    /**
     * Returns whether this instance is in high consumption mode.
     * 
     * @return true if in high consumption mode.
     */
    boolean isHigh();
}

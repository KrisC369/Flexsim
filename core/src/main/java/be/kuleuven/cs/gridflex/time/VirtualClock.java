package be.kuleuven.cs.gridflex.time;

/**
 * Interface for time keeper classes.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@FunctionalInterface
public interface VirtualClock {

    /**
     * Get the time count for this clock.
     *
     * @return The amount of times this clock has had a time step added.
     */
    int getTimeCount();
}

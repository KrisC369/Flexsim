package domain.workstation;

import domain.resource.IResource;

/**
 * This interface represents the methods that the station states can call on the
 * context class.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
interface IStationContext {

    /**
     * Change the state of this Station context instance to the working state.
     */
    void setProcessingState();

    /**
     * Change the state of this Station context instance to the idle state.
     */
    void setResourceMovingState();

    /**
     * This method pushes the represented conveyer belt forward. Resources get
     * pulled from the in-buffer if there is room in the station and finished
     * resources can be pushed to the out-buffer when finished.
     * 
     * @return true, if successful
     */
    boolean pushConveyer();

    /**
     * This method returns the resource currently present in the workstation.
     * 
     * @return the current resource
     */
    IResource getCurrentResource();
}

package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * This class represents the commonalities of the state specific behavior.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
abstract class StationStateImpl implements StationState {
    private final int varConsumptionRate;

    StationStateImpl(int varConsumption) {
        this.varConsumptionRate = varConsumption;
    }

    @Override
    public int getVarConsumptionRate() {
        return varConsumptionRate;
    }

    /**
     * The Class ProcessingState.
     */
    static final class Processing extends StationStateImpl {

        Processing(int consumption) {
            super(consumption);
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#handleTick(domain.IStationContext)
         */
        @Override
        public void handleTick(WorkstationContext context) {
            context.processResources(1);
            if (!context.hasUnfinishedResources()) {
                changestate(context);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#isProcessing()
         */
        @Override
        public boolean isProcessing() {
            return true;
        }

        private void changestate(WorkstationContext context) {
            context.setResourceMovingState();
        }

    }

    static final class ResourceMoving extends StationStateImpl {

        ResourceMoving(int consumption) {
            super(consumption);
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#handleTick(domain.IStationContext)
         */
        @Override
        public void handleTick(WorkstationContext context) {
            boolean succesfull = context.pushConveyer();
            if (succesfull) {
                changeState(context);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#isProcessing()
         */
        @Override
        public boolean isProcessing() {
            return false;
        }

        private void changeState(WorkstationContext context) {
            context.setProcessingState();
        }
    }
}

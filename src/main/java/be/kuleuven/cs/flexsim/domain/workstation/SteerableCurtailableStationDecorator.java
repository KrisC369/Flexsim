package be.kuleuven.cs.flexsim.domain.workstation;

import be.kuleuven.cs.flexsim.domain.resource.Resource;

/**
 * A decorator for decorating workstation instances that are both Steerable and
 * curtailable.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 *         TODO test the decorator hierarchies.
 */
public class SteerableCurtailableStationDecorator extends
        ForwardingStationDecorator<ConfigurableWorkstation> implements
        CurtailableWorkstation, SteerableWorkstation {

    private final CurtailableWorkstation cs;
    private int speedfactor;

    SteerableCurtailableStationDecorator(ConfigurableWorkstation ws) {
        super(ws);
        this.cs = new CurtailableStationDecorator(this);
        getDelegate().setProcessor(new SteerableProcessor());
    }

    @Override
    public void favorSpeedOverFixedEConsumption(int consumptionShift,
            int speedShift) {
        getDelegate().increaseFixedECons(consumptionShift);
        getDelegate().decreaseRatedMaxVarECons(consumptionShift);
        setProcessingSpeed(getProcessingSpeed() + speedShift);
    }

    @Override
    public void favorFixedEConsumptionOverSpeed(int consumptionShift,
            int speedShift) {
        getDelegate().decreaseFixedECons(consumptionShift);
        getDelegate().increaseRatedMaxVarECons(consumptionShift);
        setProcessingSpeed(getProcessingSpeed() - speedShift);
    }

    @Override
    public void doFullCurtailment() {
        getCurtDelegate().doFullCurtailment();

    }

    @Override
    public void restore() {
        getCurtDelegate().restore();

    }

    @Override
    public boolean isCurtailed() {
        return getCurtDelegate().isCurtailed();
    }

    /**
     * @return the curtailable instance delegate.
     */
    CurtailableWorkstation getCurtDelegate() {
        return cs;
    }

    @Override
    public void registerWith(Registerable subject) {
        subject.register((CurtailableWorkstation) this);
        subject.register((SteerableWorkstation) this);
    }

    private void setProcessingSpeed(int i) {
        this.speedfactor = i;
    }

    private int getProcessingSpeed() {
        return speedfactor;
    }

    private final class SteerableProcessor implements Processor {
        @Override
        public void doProcessingStep(Resource r, int baseSteps) {
            r.process(baseSteps + getProcessingSpeed());
        }
    }
}

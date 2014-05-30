package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * A decorator for decorating workstation instances that are both Steerable and
 * curtailable.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 *         TODO test the decorator hierarchies.
 */
public class SteerableCurtailableStationDecorator extends
        ForwardingStationDecorator<SteerableWorkstation> implements
        CurtailableWorkstation, SteerableWorkstation {

    private final CurtailableWorkstation cs;

    SteerableCurtailableStationDecorator(SteerableWorkstation ws) {
        super(ws);
        this.cs = new CurtailableStationDecorator(this);
    }

    @Override
    public void favorSpeedOverFixedEConsumption(int consumptionShift,
            int speedShift) {
        getDelegate().favorSpeedOverFixedEConsumption(consumptionShift,
                speedShift);

    }

    @Override
    public void favorFixedEConsumptionOverSpeed(int consumptionShift,
            int speedShift) {
        getDelegate().favorFixedEConsumptionOverSpeed(consumptionShift,
                speedShift);

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

}

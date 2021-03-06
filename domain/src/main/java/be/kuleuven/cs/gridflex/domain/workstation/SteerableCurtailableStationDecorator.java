package be.kuleuven.cs.gridflex.domain.workstation;

/**
 * A decorator for decorating workstation instances that are both Steerable and
 * curtailable.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class SteerableCurtailableStationDecorator
        extends SteerableStationDecorator implements CurtailableWorkstation {

    private final CurtailableWorkstation cs;

    SteerableCurtailableStationDecorator(final ConfigurableWorkstation ws) {
        super(ws);
        this.cs = new CurtailableStationDecorator<Workstation>(ws);
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

    @Override
    public void tick(final int t) {
        getCurtDelegate().tick(t);
    }

    @Override
    public void afterTick(final int t) {
        getCurtDelegate().afterTick(t);
    }

    /**
     * @return the curtailable instance delegate.
     */
    CurtailableWorkstation getCurtDelegate() {
        return cs;
    }

    @Override
    public void acceptVisitor(final WorkstationVisitor subject) {
        subject.register((CurtailableWorkstation) this);
        super.acceptVisitor(subject);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(40);
        builder.append("SteerableCurtailableWorkstation [").append(", hc=")
                .append(this.hashCode()).append("]");
        return builder.toString();
    }
}

package be.kuleuven.cs.flexsim.domain.energy.dso;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.protocol.contractnet.CNPInitiator;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * Entity that solves congestion on local distribution grids by contracting DSM
 * partners and other solutions.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public abstract class AbstractCongestionSolver implements SimulationComponent {
    protected final static int DSM_ALLOCATION_DURATION = 4 * 2;
    private final int relativeMaxValuePercent;

    private final CongestionProfile congestion;
    private final List<DSMPartner> dsms;
    private int tick;
    private final int forecastHorizon;
    private BigDecimal remediedCongestionCount;
    private final CongestionProfile afterDSMprofile;
    private double[] horizon;

    /**
     * Default constructor.
     * 
     * @param profile
     *            The congestion profile to solve.
     * @param forecastHorizon
     *            The forecast horizon.
     * @param maxRelativeValue
     *            The maximum value between (1-100) as a percent of the maximum
     *            reference energy amount that should be ignored by the
     *            mechanism. The maximum reference energy amount is defined as
     *            the peak power rate in the profile times the forecast horizon.
     */
    public AbstractCongestionSolver(CongestionProfile profile,
            int forecastHorizon, int maxRelativeValue) {
        this.congestion = profile;
        this.dsms = Lists.newArrayList();
        this.tick = 0;
        this.forecastHorizon = forecastHorizon;
        this.remediedCongestionCount = BigDecimal.ZERO;
        this.afterDSMprofile = CongestionProfile.createFromTimeSeries(profile);
        this.horizon = new double[DSM_ALLOCATION_DURATION];
        this.relativeMaxValuePercent = maxRelativeValue;
    }

    /**
     * Register this dsm partner to this solver instance.
     * 
     * @param dsm
     *            the partner to add.
     */
    public void registerDSMPartner(DSMPartner dsm) {
        dsms.add(dsm);
        getSolverInstance().registerResponder(dsm.getDSMAPI());
    }

    @Override
    public void initialize(SimulationContext context) {
    }

    @Override
    public void afterTick(int t) {
        getWorkResults();
        incrementTick();
        updateHorizon();
    }

    @Override
    public void tick(int t) {
        doTick();
    }

    private void getWorkResults() {
        double toCorrect = afterDSMprofile.value(getTick());
        for (DSMPartner d : getDsms()) {
            double dsmv = d.getCurtailment(getTick()) / 4.0;
            if (dsmv < 0) {
                throw new IllegalStateException(
                        "curtail power cannot be negative");
            }
            if (this.remediedCongestionCount.signum() < 0) {
                throw new IllegalStateException(
                        "Remedied congestion value cannot become negative");
            }
            if (toCorrect > 0) {
                if (dsmv >= toCorrect) {
                    this.remediedCongestionCount = this.remediedCongestionCount
                            .add(BigDecimal.valueOf(toCorrect));
                    toCorrect = 0;
                } else {
                    this.remediedCongestionCount = this.remediedCongestionCount
                            .add(BigDecimal.valueOf(dsmv));
                    toCorrect -= dsmv;
                }
            }
            this.afterDSMprofile.changeValue(getTick(),
                    afterDSMprofile.value(getTick()) - dsmv);
        }
    }

    private void updateHorizon() {
        this.horizon = new double[DSM_ALLOCATION_DURATION];
        for (int i = 0; i < FastMath.min(getForecastHorizon(),
                afterDSMprofile.length() - getTick() - 1); i++) {
            double toCorrect = afterDSMprofile.value(getTick() + i);
            double correction = 0;
            for (DSMPartner d : getDsms()) {
                correction += d.getCurtailment(getTick() + i) / 4.0;
            }

            horizon[i] = FastMath.max(0, toCorrect - correction);
        }
    }

    double[] getHorizon() {
        return this.horizon;
    }

    private void doTick() {
        getSolverInstance().sollicitWork();
    }

    /**
     * @return the tick
     */
    protected final int getTick() {
        return tick;
    }

    /**
     * @param tick
     *            the tick to set
     */
    private final void incrementTick() {
        this.tick = tick + 1;
    }

    /**
     * @return the congestion
     */
    public CongestionProfile getCongestion() {
        return this.congestion;
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return getDsms();
    }

    /**
     * @return the dsms
     */
    public List<DSMPartner> getDsms() {
        return Lists.newArrayList(this.dsms);
    }

    /**
     * @return the solverInstance
     */
    protected abstract CNPInitiator<DSMProposal> getSolverInstance();

    /**
     * @return the forecastHorizon
     */
    public int getForecastHorizon() {
        return this.forecastHorizon;
    }

    /**
     * @return Returns the total remedied congestion so far.
     */
    public double getTotalRemediedCongestion() {
        return this.remediedCongestionCount.doubleValue();
    }

    /**
     * @return Returns the remaining congestion profile after application of
     *         dsm.
     */
    public CongestionProfile getProfileAfterDSM() {
        return CongestionProfile.createFromTimeSeries(afterDSMprofile);
    }

    protected CongestionProfile getModifiableProfileAfterDSM() {
        return this.afterDSMprofile;
    }

    /**
     * Get a description of the work that is required in the form of a DSM
     * proposal. Can also provide an empty optional value if no work is
     * required.
     * 
     * @return an Optional containing a dsm proposal for work in this time
     *         period or an empty value.
     */
    protected Optional<DSMProposal> getWorkProposal() {
        double cong = getCongestion().value(getTick());
        double sum = 0;
        Min m = new Min();
        m.setData(new double[] { 8, getCongestion().length() - getTick() - 1 });
        for (int i = 0; i < m.evaluate(); i++) {
            sum += getHorizon()[i];
        }
        if ((sum / (getCongestion().max() * 8.0)
                * 100) < relativeMaxValuePercent) {
            return Optional.absent();
        }

        return Optional.fromNullable(DSMProposal.create(
                "CNP for activation for tick: " + getTick(), cong, 0, getTick(),
                getTick() + DSM_ALLOCATION_DURATION));
    }
}

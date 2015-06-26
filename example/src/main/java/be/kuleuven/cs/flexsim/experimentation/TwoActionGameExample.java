package be.kuleuven.cs.flexsim.experimentation;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregationStrategyImpl;
import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.aggregation.reactive.ReactiveMechanismAggregator;
import be.kuleuven.cs.flexsim.domain.energy.generation.ConstantOutputGenerator;
import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.generation.WeighedNormalRandomOutputGenerator;
import be.kuleuven.cs.flexsim.domain.energy.tso.contractual.BalancingTSO;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTrackerImpl;
import be.kuleuven.cs.flexsim.domain.site.Site;

/**
 * Represents a game with two possible actions to choose between.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class TwoActionGameExample extends AggregationGame<Site, Aggregator> {

    private static final int ACTIONSPACE_SIZE = 2;
    private static final int ACTPAYMENT = 50;
    private final BalancingTSO tso;
    private int count;
    private final int baseConsumption;
    private final double factor;

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed
     *            The seed for this experiment.
     * @param baselineConsumption
     *            The baseline for the sites consumption. This is used to base
     *            production params on.
     * @param factor
     *            The retribution factor.
     */
    public TwoActionGameExample(int seed, int baselineConsumption, double factor) {
        super(seed);
        this.tso = new BalancingTSO();
        this.count = 0;
        this.baseConsumption = baselineConsumption;
        addAggregator(new ReactiveMechanismAggregator(tso,
                AggregationStrategyImpl.CARTESIANPRODUCT));
        addAggregator(new ReactiveMechanismAggregator(tso,
                AggregationStrategyImpl.MOVINGHORIZON));
        this.factor = factor;
    }

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed
     *            The seed for this experiment.
     * @param baselineConsumption
     *            The baseline for the sites consumption. This is used to base
     *            production params on.
     */
    public TwoActionGameExample(int seed, int baselineConsumption) {
        this(seed, baselineConsumption, 1);

    }

    @Override
    public void fixActionToAgent(Site agent, Aggregator action) {
        addSite(agent);
        tso.registerConsumer(agent);
        addChoice(agent, action);
        FinanceTrackerImpl fti;
        if (getAggregators().get(0).equals(action)) {
            fti = (FinanceTrackerImpl) FinanceTrackerImpl
                    .createCustomBalancingFeeTracker(agent, ACTPAYMENT,
                            this.factor);
        } else {
            fti = (FinanceTrackerImpl) FinanceTrackerImpl
                    .createCustomBalancingFeeTracker(agent, ACTPAYMENT, 0);
        }
        addFinanceTracker(fti);
        action.registerClient(agent);
        this.count++;
    }

    @Override
    public void init() {
        // Add finance trackers keeping track of profit and consumptions.
        EnergyProductionTrackable p1 = new ConstantOutputGenerator(
                baseConsumption * getNumberOfAgents());
        EnergyProductionTrackable p2 = new WeighedNormalRandomOutputGenerator(
                -1500, 1500, 0.010);
        for (Aggregator agg : this.getAggregators()) {
            addSimComponent(agg);
        }
        tso.registerProducer(p1);
        tso.registerProducer(p2);
        addSimComponent(tso);
    }

    private int getNumberOfAgents() {
        return count;
    }

    /**
     * @return the actionspacesize
     */
    public static final int getActionspacesize() {
        return ACTIONSPACE_SIZE;
    }

    @Override
    public long getExternalityValue() {
        return 0;
    }
}

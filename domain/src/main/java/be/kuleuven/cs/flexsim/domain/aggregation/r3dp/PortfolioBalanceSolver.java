package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.PowerValuesProfile;

/**
 * Represents a portfolio balancing entity that solves intraday imbalances because of prediction
 * error in portfolios.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PortfolioBalanceSolver extends DistributionGridCongestionSolver {

    //    private final CableCurrentProfile imbalance;
    //    private final TurbineSpecification turbineSpec;

    /**
     * Default constructor
     *
     * @param fac The solver factory to draw solver from.
     * @param c   The initial imbalance profile to transform to imbalances.
     */
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            CableCurrentProfile c, TurbineSpecification specs) {
        super(fac, convertProfile(c, specs));
        //        this.turbineSpec = specs;
        //        imbalance = calculateImbalanceFromActual(
        //                toPowerValues(applyPredictionErrors(toWindSpeed(c, turbineSpec)),
        // turbineSpec),
        //                c);
    }

    static PowerValuesProfile convertProfile(CableCurrentProfile c, TurbineSpecification specs) {
        return new TurbineProfileConvertor(c, specs).convertProfileWith();
    }
}

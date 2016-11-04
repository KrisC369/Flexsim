package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.tosg.ExperimentParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.WindBasedInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.stat.EgtResultParser;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGame;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import com.google.common.collect.ImmutableList;
import org.jppf.node.protocol.Task;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static be.kuleuven.cs.flexsim.experimentation.tosg.WgmfInputParser.parseInputAndExec;
import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.REMOTE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Game runner for wgmf games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfGameRunner {
    private static final String DISTRIBUTIONFILE = "windspeedDistributions.csv";
    private static final String DATAFILE = "2kwartOpEnNeer.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices.csv";
    private static final String PARAMS_KEY = "PARAMS_KEY";
    private static final int ACTION_SIZE = 2;
    private static final ExecutionStrategy EXECUTION_STRATEGY_DEFAULT = REMOTE;
    private final ConfigurableGameDirector director;
    private static final Logger logger = getLogger(WgmfGameRunner.class);

    private final ExecutionStrategy strategy;

    public WgmfGameRunner(ExperimentParams expP, ExecutionStrategy strat) {
        ConfigurableGame game = new ConfigurableGame(expP.getNAgents(),
                ACTION_SIZE, expP.getNRepititions());
        this.director = new ConfigurableGameDirector(game);
        this.strategy = strat;
    }

    private WgmfGameRunner(ExperimentParams expP) {
        this(expP, expP.runRemote() ? ExecutionStrategy.REMOTE : ExecutionStrategy.LOCAL);
    }

    public static void main(String[] args) {
        ExperimentParams expP = parseInputAndExec(args);
        WgmfGameRunner gameJPPFRunner = new WgmfGameRunner(expP);
        WgmfGameParams params = loadResources(expP);
        gameJPPFRunner.execute(params);
    }

    public static WgmfGameParams loadResources(ExperimentParams expP) {
        try {
            WindBasedInputData dataIn = WindBasedInputData.loadFromResource(DATAFILE);
            TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);
            ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(IMBAL);
            ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                    .loadFromCSV(DISTRIBUTIONFILE);
            return WgmfGameParams
                    .create(dataIn, new WgmfSolverFactory(expP.getSolver()), specs, distribution,
                            imbalIn);
        } catch (IOException e) {
            throw new IllegalStateException("One of the resources could not be loaded.", e);
        }
    }

    public void execute(WgmfGameParams params) {
        List<WgmfJppfTask> adapted = strategy.adapt(director, params, PARAMS_KEY);
        ExperimentRunner runner = strategy.getRunner(params, PARAMS_KEY);
        runner.runExperiments(adapted);
        List<Task<?>> results = runner.waitAndGetResults();
        logger.info("Experiment results received. \nProcessing results... ");
        strategy.processExecutionResults(results, director);
        logResults();
    }

    private void logResults() {
        try (EgtResultParser egtResultParser = new EgtResultParser(null)) {
            ImmutableList<Double> eqnParams = director.getResults().getResults();
            double[] fixedPoints = egtResultParser
                    .findFixedPointForDynEquationParams(
                            eqnParams.stream().mapToDouble(Double::doubleValue).toArray());
            logger.warn("Phase plot fixed points found at: " + Arrays.toString(fixedPoints));
        } catch (Exception e) {
            logger.error("Something went wrong parsing the results", e);
        }
        logger.warn("Dynamics equation params: " + director.getDynamicEquationArguments());
        logger.warn("Payoff table: \n" + director.getFormattedResults().getFormattedResultString());
    }
}

package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.tosg.ExperimentParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WhoGetsMyFlexGame;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.CsvResultWriter;
import be.kuleuven.cs.flexsim.experimentation.tosg.stat.EgtResultParser;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGame;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import be.kuleuven.cs.gametheory.evolutionary.EvolutionaryGameDynamics;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections15.map.UnmodifiableMap;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.LOCAL;
import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.REMOTE;
import static com.google.common.base.Preconditions.checkArgument;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Game runner for wgmf games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfGameRunnerVariableDistributionCosts extends AbstractWgmfGameRunner {
    private static final Logger logger = getLogger(WgmfGameRunnerVariableDistributionCosts.class);
    public static final String PRICE_PARAM_KEY = "DISTRIBUTION_E_S_PRICE";
    public static final EvolutionaryGameDynamics.ConfidenceLevel CI_LEVEL = EvolutionaryGameDynamics
            .ConfidenceLevel._95pc;
    private static final String RES_OUTPUT_FILE = "res_outputN2";
    //    private final ConfigurableGameDirector director;

    private int nAgents, nReps;
    private final Map<Double, ConfigurableGameDirector> priceToDirector;
    private final LinkedListMultimap<ConfigurableGameDirector, WgmfJppfTask> directorToTasks;
    private double minPrice;
    private double maxPrice;
    private double priceStep;

    /**
     * Public constructor from params object and exec strategy.
     *
     * @param expP  The experiment parameters.
     * @param strat The execution strategy.
     */
    public WgmfGameRunnerVariableDistributionCosts(ExperimentParams expP) {
        super(expP.isRemoteExecutable() ? REMOTE : LOCAL);
        this.nAgents = expP.getNAgents();
        this.nReps = expP.getNRepititions();
        ConfigurableGame game = new ConfigurableGame(expP.getNAgents(),
                ACTION_SIZE, expP.getNRepititions());
        //        this.director = new ConfigurableGameDirector(game);
        priceToDirector = Maps.newLinkedHashMap();
        directorToTasks = LinkedListMultimap.create();
        this.minPrice = expP.getP1Start();
        this.priceStep = expP.getP1Step();
        this.maxPrice = expP.getP1End();
    }

    /**
     * Main method. Start execution at this point.
     *
     * @param args The arguments passed.
     */
    public static void main(String[] args) {
        startExecution(args, (params) -> new WgmfGameRunnerVariableDistributionCosts(params));
    }

    @Override
    protected void execute(WgmfGameParams params) {
        List<WgmfJppfTask> alltasks = Lists.newArrayList();
        for (double p = minPrice; p <= maxPrice; p += priceStep) {
            final double price = p;
            ConfigurableGame game = new ConfigurableGame(nAgents,
                    ACTION_SIZE, nReps);
            ConfigurableGameDirector director = new ConfigurableGameDirector(game);
            priceToDirector.put(price, director);

            List<GameInstanceConfiguration> priceContainingConfigs =
                    getConfigsWithPricesFromDirector(price, director);

            List<WgmfJppfTask> adapted = adaptPriceConfigsToRunnableTasks(params,
                    priceContainingConfigs);
            directorToTasks.putAll(director, adapted);
            alltasks.addAll(adapted);
        }

        ExperimentRunner runner = getStrategy().getRunner(params, PARAMS_KEY);
        runner.runExperiments(alltasks);
        List<?> results = runner.waitAndGetResults();
        logger.info("Experiment results received. \nProcessing results... ");
        getStrategy().processExecutionResults(results, PRICE_PARAM_KEY,
                UnmodifiableMap.decorate(priceToDirector));
    }

    private List<WgmfJppfTask> adaptPriceConfigsToRunnableTasks(WgmfGameParams params,
            List<GameInstanceConfiguration> priceContainingConfigs) {
        return getStrategy().adapt(priceContainingConfigs, params, PARAMS_KEY,
                (WgmfGameParams wgmfParams, GameInstanceConfiguration instanceConfig) ->
                        WhoGetsMyFlexGame
                                .createVariableDSOPricingGame(wgmfParams, instanceConfig.getSeed(),
                                        instanceConfig.getExtraConfigValues()
                                                .get(PRICE_PARAM_KEY)));
    }

    private List<GameInstanceConfiguration> getConfigsWithPricesFromDirector(double price,
            ConfigurableGameDirector director) {
        return director.getPlayableVersions().stream()
                .map((config) -> config.withExtraConfigValue(PRICE_PARAM_KEY, price))
                .collect(Collectors.toList());
    }

    @Override
    protected void processResults() {
        List<CsvResultWriter.WgmfDynamicsResults> toWrite = Lists.newArrayList();
        try (EgtResultParser egtResultParser = new EgtResultParser(null)) {
            for (Map.Entry<Double, ConfigurableGameDirector> entry : priceToDirector.entrySet()) {
                EvolutionaryGameDynamics dynamics = EvolutionaryGameDynamics
                        .from(entry.getValue().getResults().getResults());
                double[] eqnParams = dynamics.getDynamicEquationFactors().stream()
                        .mapToDouble(Double::doubleValue).toArray();
                double[] lowerCI = getLowerCIParams(dynamics).stream()
                        .mapToDouble(Double::doubleValue).toArray();
                double[] higherCI = getHigherCIParams(dynamics).stream()
                        .mapToDouble(Double::doubleValue).toArray();
                double[] fixedPoints = egtResultParser
                        .findFixedPointForDynEquationParams(eqnParams);
                double[] fixedPointsLower = egtResultParser
                        .findFixedPointForDynEquationParams(lowerCI);
                double[] fixedPointsHigher = egtResultParser
                        .findFixedPointForDynEquationParams(higherCI);
                logger.warn("Results for pricepoint {}:", entry.getKey());
                logger.warn("Dynamics equation params: {}",
                        entry.getValue().getDynamicEquationArguments());
                logger.warn("Phase plot fixed points found at: {}", Arrays.toString(fixedPoints));
                logger.warn("{} CI Upper bound Phase plot fixed points found at: {}",
                        CI_LEVEL.getConfidenceLevel(), Arrays.toString(fixedPointsHigher));
                logger.warn("{} CI Lower bound Phase plot fixed points found at: {}",
                        CI_LEVEL.getConfidenceLevel(), Arrays.toString(fixedPointsHigher));
                toWrite.add(CsvResultWriter.WgmfDynamicsResults
                        .create(nAgents, entry.getKey(), fixedPoints, fixedPointsLower,
                                fixedPointsHigher));
            }
            CsvResultWriter.writeCsvFile(
                    RES_OUTPUT_FILE + String.valueOf(System.currentTimeMillis() / 100), toWrite);
        } catch (Exception e) {
            logger.error("Something went wrong parsing the results", e);
            throw new RuntimeException(e);
        }
    }

    private List<Double> getHigherCIParams(EvolutionaryGameDynamics dynamics) {
        return getGenericCIParams(dynamics, ci -> ci.getLowerBound(), ci -> ci.getUpperBound());
    }

    private List<Double> getLowerCIParams(EvolutionaryGameDynamics dynamics) {
        return getGenericCIParams(dynamics, ci -> ci.getUpperBound(), ci -> ci.getLowerBound());
    }

    private List<Double> getGenericCIParams(EvolutionaryGameDynamics dynamics,
            ToDoubleFunction<ConfidenceInterval> first, ToDoubleFunction<ConfidenceInterval> last) {
        List<ConfidenceInterval> cis = dynamics.getConfidenceIntervals(CI_LEVEL);
        checkArgument(!cis.isEmpty(), "Dynamics should not be empty.");
        List<Double> toRet = Lists.newArrayList();
        toRet.add(first.applyAsDouble(cis.get(0)));
        for (int i = 1; i < cis.size() - 1; i++) {
            if (i % 2 != 0) {
                toRet.add(first.applyAsDouble(cis.get(i)));
            } else {
                toRet.add(last.applyAsDouble(cis.get(i)));
            }
        }
        toRet.add(last.applyAsDouble(cis.get(cis.size() - 1)));
        return toRet;
    }
}
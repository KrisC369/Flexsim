package be.kuleuven.cs.gridflex.solvers.heuristic.solver;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.solvers.data.AllocResults;
import be.kuleuven.cs.gridflex.solvers.heuristic.domain.ActivationAssignment;
import be.kuleuven.cs.gridflex.solvers.heuristic.domain.Allocation;
import be.kuleuven.cs.gridflex.solvers.heuristic.domain.OptaFlexProvider;
import be.kuleuven.cs.gridflex.solvers.heuristic.domain.QHFlexibilityProvider;
import com.google.auto.value.AutoValue;
import com.google.common.base.Charsets;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.Resources;
import org.eclipse.jdt.annotation.Nullable;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationCompositionStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Heuristic solvers making use of optaplanner to find a solution to flex allocation problems.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class HeuristicSolver implements Solver<AllocResults> {

    private static final int DECIMAL_SCALING = 100;
    private static String CONFIG_FULLSAT =
            "be/kuleuven/cs/gridflex/solvers/heuristic/solver/HeuristicSolverConfig_FullSat.xml";
    private static String CONFIG_BESTEFFORT =
            "be/kuleuven/cs/gridflex/solvers/heuristic/solver/HeuristicSolverConfig_BestEffort.xml";
    private static double FRACTION_OF_THEORETICAL_OPT = 0.90;
    private final FlexAllocProblemContext context;
    private final boolean fullSat;
    @Nullable
    private SolveResult solvedAllocResult;
    private static final Logger logger = LoggerFactory.getLogger(HeuristicSolver.class);
    private final long randomSeed;

    HeuristicSolver(FlexAllocProblemContext context, boolean fullSat) {
        this.context = context;
        this.fullSat = fullSat;
        this.randomSeed = context.getSeedValue();
    }

    @Override
    public AllocResults solve() {
        doSolve();
        return getSolution();
    }

    private void doSolve() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting solve run.");
        }
        String actConfig = fullSat ? CONFIG_FULLSAT : CONFIG_BESTEFFORT;

        if (logger.isDebugEnabled()) {
            logger.debug("Reading xml config at location: {}", actConfig);
        }
        SolverFactory<Allocation> solverFactory = SolverFactory
                .createFromXmlReader(new StringReader(resourceToString(actConfig)));

        //config
        SolverConfig solverConfig = solverFactory.getSolverConfig();

        //Random seed
        solverConfig.setRandomSeed(randomSeed);

        //Termination rules.
        //        solverConfig.getTerminationConfig().setMinutesSpentLimit(10L);
        //        solverConfig.getTerminationConfig().setStepCountLimit(500);

        double sum = Lists.newArrayList(context.getProviders()).stream()
                .mapToDouble(p -> p.getFlexibilityActivationRate().getUp() * p
                        .getFlexibilityActivationConstraints().getMaximumActivations() * p
                        .getFlexibilityActivationConstraints().getActivationDuration()).sum();
        double bestScore = sum * FRACTION_OF_THEORETICAL_OPT * DECIMAL_SCALING;

        String bestScoreString = "0hard/" + String.valueOf((int) bestScore) + "soft";
        solverConfig.getTerminationConfig()
                .setBestScoreLimit(bestScoreString);
        solverConfig.getTerminationConfig().setTerminationCompositionStyle(
                TerminationCompositionStyle.OR);
        //        solverConfig.getTerminationConfig().setUnimprovedStepCountLimit(500);

        if (logger.isDebugEnabled()) {
            logger.info(
                    "Profile loaded with a total of {} units as a volume to resolve with {} units"
                            + " of flexible allocation available.",
                    context.getEnergyProfileToMinimizeWithFlex().sum(), sum);
            if (context.getEnergyProfileToMinimizeWithFlex().sum() < sum) {
                logger.info("Note that the maximum available flex is higher than the needed flex.");
            }
            logger.debug(
                    "Minimum best score: {} added to termination condition. This is {}% relative "
                            + "efficiency of theoretical max: {}",
                    bestScoreString, FRACTION_OF_THEORETICAL_OPT * 100, sum * 100);
        }

        //Build solvers
        org.optaplanner.core.api.solver.Solver<Allocation> solver = solverFactory.buildSolver();
        Allocation unsolvedAlloc = new AllocationGenerator().createAllocation();

        // Solve the problem
        if (logger.isDebugEnabled()) {
            logger.debug("Starting search.", actConfig);
        }
        solver.solve(unsolvedAlloc);
        Allocation solvedAlloc = (Allocation) solver.getBestSolution();
        double objValue = solvedAlloc.getResolvedCongestion();
        double relative = objValue / sum;

        if (logger.isInfoEnabled()) {
            logger.info("Search ended with objective function result: {}.",
                    objValue);
            logger.info("Obj value percentage of theoretical max useful allocation: {}",
                    relative);
        }
        solvedAllocResult = SolveResult.create(solvedAlloc, objValue, relative);
    }

    public void displayResult() {
        logger.info("\nSolved with value: {}", toDisplayString(solvedAllocResult.getAllocation()));
    }

    AllocResults getSolution() {
        Allocation solvedAlloc = solvedAllocResult.getAllocation();
        List<QHFlexibilityProvider> providers = solvedAlloc.getProviders();
        int[][] allocationMaps = solvedAlloc.getAllocationMaps();
        ListMultimap<FlexibilityProvider, Boolean> actMap = MultimapBuilder
                .linkedHashKeys(context.getProviders().size())
                .arrayListValues(context.getEnergyProfileToMinimizeWithFlex().length())
                .build();
        for (int i = 0; i < allocationMaps.length; i++) {
            List<Boolean> toAdd = Lists.newArrayList();
            for (int j = 0; j < allocationMaps[i].length; j++) {
                toAdd.add(allocationMaps[i][j] == 1 ? true : false);
            }
            actMap.putAll(providers.get(i).getWrappedProvider(), toAdd);
        }
        if (solvedAlloc.getScore().isFeasible()) {
            return AllocResults.create(actMap, solvedAlloc.getResolvedCongestion(),
                    solvedAllocResult.getRelativeObj());
        } else {
            return AllocResults.INFEASIBLE;
        }
    }

    public class AllocationGenerator {
        public Allocation createAllocation() {
            List<QHFlexibilityProvider> providers = Lists.newArrayList();
            //Order preserving needed.
            //            context.getProviders().forEach(p -> providers.add(new OptaFlexProvider
            // (p)));
            for (FlexibilityProvider p : context.getProviders()) {
                providers.add(new OptaFlexProvider(p));
            }

            List<ActivationAssignment> assignments = Lists.newArrayList();
            CongestionProfile profile = CongestionProfile
                    .createFromTimeSeries(context.getEnergyProfileToMinimizeWithFlex());
            int id = 0;
            for (QHFlexibilityProvider prov : providers) {
                for (int i = 0;
                     i < prov.getQHFlexibilityActivationConstraints()
                             .getMaximumActivations(); i++) {
                    ActivationAssignment actAss = ActivationAssignment
                            .create(id++, prov, profile);
                    assignments.add(actAss);
                }
            }
            Allocation a = new Allocation();
            a.setAssignments(assignments);
            a.setProfile(profile);
            a.setProviders(providers);
            a.setActivationStartValues(IntStream.range(0, profile.length()).boxed()
                    .collect(Collectors.toList()));
            return a;
        }
    }

    /**
     * Display the allocation results' to string.
     *
     * @param allocation The allocation to print
     * @return A string containing the resolved congestion for this allocation.
     */
    public static String toDisplayString(Allocation allocation) {
        double sum = allocation.getResolvedCongestion();
        StringBuilder displayString = new StringBuilder(19);
        displayString.append("Solved Congestion: ").append(sum);
        return displayString.toString();
    }

    static String resourceToString(String resourcePath) {
        try {
            final URL url = Resources.getResource(resourcePath);
            return Resources.toString(url, Charsets.UTF_8);
        } catch (final IOException e) {
            throw new IllegalArgumentException(
                    "A problem occured while attempting to read: " + resourcePath, e);
        }
    }

    public static HeuristicSolver createFullSatHeuristicSolver(FlexAllocProblemContext context) {
        return new HeuristicSolver(context, true);
    }

    public static HeuristicSolver createBestEffortHeuristicSolver(FlexAllocProblemContext context) {
        return new HeuristicSolver(context, false);
    }

    @AutoValue
    static abstract class SolveResult {
        abstract Allocation getAllocation();

        abstract double getObjectiveValue();

        abstract double getRelativeObj();

        static SolveResult create(Allocation a, double obj, double rel) {
            return new AutoValue_HeuristicSolver_SolveResult(a, obj, rel);
        }
    }
}
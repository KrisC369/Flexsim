package be.kuleuven.cs.flexsim.experimentation.tosg.poc;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.DistributionGridCongestionSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;
import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import be.kuleuven.cs.flexsim.solver.optimal.dso.DSOOptimalSolver;
import be.kuleuven.cs.gametheory.Game;
import be.kuleuven.cs.gametheory.GameConfigurator;
import be.kuleuven.cs.gametheory.GameDirector;
import be.kuleuven.cs.gametheory.GameInstance;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PoCRunner {

    public static void main(String[] args) {
        PoCConfigurator poCConfigurator = new PoCConfigurator();
        Game<FlexibilityProvider, FlexibilityUtiliser> game = new Game<>(2, poCConfigurator, 1);
        GameDirector director = new GameDirector(game);
        director.playAutonomously();
        System.out.println(director.getFormattedResults().getFormattedResultString());
    }

    private static final String DATAFILE = "2kwartOpEnNeer.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String COLUMN = "verlies aan energie";
    private static final int NAGENTS = 2;

    public PoCRunner() {
    }

    @Deprecated
    private void pocInstantiation() {
        TurbineSpecification specs = TurbineSpecification.empty();
        CongestionProfile c1 = CongestionProfile.empty();
        CableCurrentProfile c2 = CableCurrentProfile.empty();
        Simulator s;
        try {
            specs = TurbineSpecification.loadFromResource(SPECFILE);
            WindBasedInputData dataIn = WindBasedInputData.loadFromResource(DATAFILE);
            c1 = dataIn.getCongestionProfile();
            c2 = dataIn.getCableCurrentProfile();

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        s = Simulator.createSimulator(1000);
        AbstractSolverFactory<SolutionResults> fact = new AbstractSolverFactory<SolutionResults>
                () {
            @Override
            public Solver<SolutionResults> createSolver(FlexAllocProblemContext context) {
                return new SolverAdapter<AllocResults, SolutionResults>(
                        new DSOOptimalSolver(context, AbstractOptimalSolver.Solver.CPLEX)) {

                    @Override
                    public SolutionResults adaptResult(AllocResults solution) {
                        return SolutionResults.EMPTY;
                    }
                };
            }
        };
        FlexProvider p1 = new FlexProvider(300);
        FlexProvider p2 = new FlexProvider(300);
        PortfolioBalanceSolver tso = new PortfolioBalanceSolver(fact, c2, specs);
        DistributionGridCongestionSolver dso = new DistributionGridCongestionSolver(fact, c1);
        tso.registerFlexProvider(p1);
        dso.registerFlexProvider(p2);
        dso.solve();
        tso.solve();
        SolutionResults r1 = dso.getSolution();
        SolutionResults r2 = tso.getSolution();
        System.out.println(r1);
        System.out.println(r2);
    }

    public static class PoCConfigurator
            implements GameConfigurator<FlexibilityProvider, FlexibilityUtiliser> {
        private static final double R3DP_GAMMA_SCALE = 677.926;
        private static final double R3DP_GAMMA_SHAPE = 1.37012;
        private static final long SEED = 1312421L;
        private CongestionProfile c1;
        private CableCurrentProfile c2;
        private TurbineSpecification specs;

        final GammaDistribution gd;

        public PoCConfigurator() {
            gd = new GammaDistribution(new MersenneTwister(SEED),
                    R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
            try {
                WindBasedInputData dataIn = WindBasedInputData.loadFromResource(DATAFILE);
                specs = TurbineSpecification.loadFromResource(SPECFILE);
                c1 = dataIn.getCongestionProfile();
                c2 = dataIn.getCableCurrentProfile();
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public FlexibilityProvider getAgent() {
            return new FlexProvider(gd.sample());
        }

        @Override
        public GameInstance<FlexibilityProvider, FlexibilityUtiliser> generateInstance() {
            return new PoCGame(c1, c2, specs);
        }

        @Override
        public int getActionSpaceSize() {
            return 2;
        }
    }

    public static class PoCGame implements
                                GameInstance<FlexibilityProvider, FlexibilityUtiliser> {

        //        private final int nAgents;
        private final Set<FlexibilityProvider> agents;
        private final List<FlexibilityUtiliser> actions;

        private final Map<FlexibilityProvider, FlexibilityUtiliser> agentActionMap;
        private CongestionProfile c1;
        private CableCurrentProfile c2;
        private TurbineSpecification specs;

        public PoCGame(CongestionProfile c1, CableCurrentProfile c2, TurbineSpecification specs) {
            agents = Sets.newLinkedHashSet();
            actions = Lists.newArrayList();
            //            this.nAgents = nAgents;
            agentActionMap = Maps.newLinkedHashMap();
            this.c1 = c1;
            this.c2 = c2;
            this.specs = specs;
            AbstractSolverFactory<SolutionResults> fact = new AbstractSolverFactory<SolutionResults>
                    () {
                @Override
                public Solver<SolutionResults> createSolver(FlexAllocProblemContext context) {
                    return new SolverAdapter<AllocResults, SolutionResults>(
                            new DSOOptimalSolver(context, AbstractOptimalSolver.Solver.CPLEX)) {

                        @Override
                        public SolutionResults adaptResult(AllocResults solution) {
                            return new SolutionResultAdapter(solution).getResults();
                        }
                    };
                }
            };
            actions.add(new PortfolioBalanceSolver(fact, this.c2, specs));
            actions.add(new DistributionGridCongestionSolver(fact, this.c1));
        }

        @Override
        public Map<FlexibilityProvider, Long> getPayOffs() {
            Map<FlexibilityProvider, Long> results = Maps.newLinkedHashMap();
            agentActionMap.forEach(
                    (agent, action) -> results.put(agent, agent.getMonetaryCompensationValue()));
            return results;
        }

        @Override
        public void fixActionToAgent(FlexibilityProvider agent,
                FlexibilityUtiliser action) {
            agents.add(agent);
            agentActionMap.put(agent, action);
        }

        @Override
        public void init() {
            agentActionMap.forEach(
                    (FlexibilityProvider fp,
                            FlexibilityUtiliser action) -> action.registerFlexProvider(fp));

        }

        @Override
        public List<FlexibilityUtiliser> getActionSet() {
            return Collections.unmodifiableList(actions);
        }

        @Override
        public Map<FlexibilityProvider, FlexibilityUtiliser> getAgentToActionMapping() {
            return Collections.unmodifiableMap(agentActionMap);
        }

        @Override
        public long getExternalityValue() {
            return 0;
        }

        @Override
        public void play() {
            new SimulatedGamePlayAdapter(actions).play();
        }
    }
}

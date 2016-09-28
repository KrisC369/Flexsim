package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.experimentation.tosg.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.SolutionResultAdapter;
import be.kuleuven.cs.flexsim.experimentation.tosg.SolverAdapter;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfConfigurator;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WindBasedInputData;
import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import be.kuleuven.cs.flexsim.solver.optimal.dso.DSOOptimalSolver;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGame;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver.Solver.DUMMY;

/**
 * Experiment runner for the Who-gets-my-flex game.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class JppFWgmfGameRunner {

    private static final int SEED = 3722;
    private static final String DISTRIBUTIONFILE = "windspeedDistributions.csv";
    private static final String DATAFILE = "2kwartOpEnNeer.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices.csv";
    private static final int NAGENTS_DEFAULT = 2;
    private static final int NREPS_DEFAULT = 1;
    private static final AbstractOptimalSolver.Solver SOLVER_DEFAULT = DUMMY;
    protected static final Logger logger = LoggerFactory.getLogger(JppFWgmfGameRunner.class);
    protected final String loggerTag;
    private final AbstractOptimalSolver.Solver type;
    private final int nAgents;
    private final int repititions;

    protected JppFWgmfGameRunner(final int repititions, final int nAgents,
            AbstractOptimalSolver.Solver type) {
        this(repititions, nAgents, type, "");

    }

    protected JppFWgmfGameRunner(final int repititions, final int nAgents,
            AbstractOptimalSolver.Solver type, final String loggerTag) {
        this.type = type;
        this.loggerTag = loggerTag;
        this.nAgents = nAgents;
        this.repititions = repititions;
    }

    public ConfigurableGameDirector getGameDirectorInstance() throws IOException {
        WindBasedInputData dataIn = WindBasedInputData.loadFromResource(DATAFILE);
        TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);
        ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(IMBAL);
        ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                .loadFromCSV(DISTRIBUTIONFILE);

        WgmfConfigurator configurator = new WgmfConfigurator(
                WgmfGameParams
                        .create(dataIn, new SolverFactory(type), specs, distribution, imbalIn));
        ConfigurableGame game = new ConfigurableGame(nAgents,
                configurator.getActionSpaceSize(), repititions);
        return new ConfigurableGameDirector(game);
    }

    public static JppFWgmfGameRunner parseInputAndExec(String[] args) {
        Options o = new Options();
        o.addOption("n", true, "The number of participating agents");
        o.addOption("r", true, "The number of repititions");
        o.addOption(OptionBuilder.withLongOpt("solver")
                .withDescription("Which solver to use. [CPLEX|GUROBI]")
                .hasArg()
                .withArgName("SOLVER")
                .create("s"));

        int nAgents = NAGENTS_DEFAULT;
        int nReps = NREPS_DEFAULT;
        AbstractOptimalSolver.Solver solver = SOLVER_DEFAULT;

        CommandLineParser parser = new BasicParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(o, args);
            if (line.hasOption("n")) {
                nAgents = Integer.parseInt(line.getOptionValue("n"));
            }
            if (line.hasOption("r")) {
                nReps = Integer.parseInt(line.getOptionValue("r"));
            }
            if (line.hasOption("s")) {
                solver = AbstractOptimalSolver.Solver.valueOf(line.getOptionValue("s"));
            }
            logger.warn("Performing " + nReps + " repititions for experiment with " + nAgents
                    + " agents using: " + solver.toString());
            return new JppFWgmfGameRunner(nReps, nAgents, solver);

        } catch (ParseException exp) {
            // oops, something went wrong
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("WgmfGameRunner", o);
            logger.error("Parsing failed.  Reason: " + exp.getMessage(), exp);
            throw new IllegalStateException(exp);
        }
    }

    public class SolverFactory implements AbstractSolverFactory<SolutionResults> {
        private AbstractOptimalSolver.Solver type;

        private SolverFactory(AbstractOptimalSolver.Solver type) {
            this.type = type;
        }

        @Override
        public Solver<SolutionResults> createSolver(FlexAllocProblemContext context) {
            return new SolverAdapter<AllocResults, SolutionResults>(
                    new DSOOptimalSolver(context, type)) {

                @Override
                public SolutionResults adaptResult(AllocResults solution) {
                    return new SolutionResultAdapter(solution).getResults();
                }
            };
        }
    }
}

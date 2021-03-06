package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.ErrorDistributionType;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.solvers.Solvers;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.cli.OptionBuilder.withLongOpt;

/**
 * Input parser for who-gets-my-flex games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class WgmfInputParser {
    private static final String AGENT_KEY = "n";
    private static final String REPS_KEY = "r";
    private static final String P1START_KEY = "p1start";
    private static final String P1END_KEY = "p1end";
    private static final String P1STEP_KEY = "p1step";
    private static final String DISTINDEX_KEY = "dIdx";
    private static final String PROFINDEX_KEY = "pIdx";
    private static final String MODE_KEY = "m";
    private static final String SOLVER_KEY = "s";
    private static final String DISTRIBUTION_KEY = "distribution";
    private static final String INTERARRIVAL_KEY = "flexIA";
    private static final String DURATION_KEY = "flexDUR";
    private static final String ACTCOUNT_KEY = "flexCOUNT";

    private static final int NAGENTS_DEFAULT = 2;
    private static final int NREPS_DEFAULT = 1;
    private static final Logger logger = LoggerFactory.getLogger(WgmfInputParser.class);
    private static final Solvers.TYPE SOLVER_DEFAULT = Solvers.TYPE.DUMMY;
    private static final String CACHING_ALLOC_KEY = "c";

    private WgmfInputParser() {
    }

    /**
     * Use --help or -r [repititions] -n [numberOfAgents] -s [GUROBI|CPLEX|DUMMY|OPTA] -m
     * [LOCAL|REMOTE] -p1start [rangeStart] -p1end [rangeEnd] -p1step [stepSize]
     * -pIdx[profileIndex] -dIdx [errorDataIdx] -distribution [CAUCHY|NORMAL]
     *
     * @param args The commandline args.
     * @return The experiment params.
     */
    public static ExperimentParams parseInputAndExec(String[] args) {
        Options o = new Options();
        o.addOption(AGENT_KEY, true, "The number of participating agents");
        o.addOption(REPS_KEY, true, "The number of repititions");
        o.addOption(withLongOpt("solvers")
                .withDescription("Which solvers to use. [CPLEX|GUROBI]")
                .hasArg()
                .withArgName("SOLVER")
                .create(SOLVER_KEY));
        o.addOption(withLongOpt("mode")
                .withDescription("Which execution mode to use. [LOCAL|REMOTE]")
                .hasArg()
                .withArgName("MODE")
                .create(MODE_KEY));
        o.addOption(P1START_KEY, true, "The starting value of first param");
        o.addOption(P1STEP_KEY, true, "The step size of first param");
        o.addOption(P1END_KEY, true, "The end value of first param");
        o.addOption(INTERARRIVAL_KEY, true,
                "Flexibility constraints parameter: min time between activations");
        o.addOption(DURATION_KEY, true,
                "Flexibility constraints parameter: duration of an activation");
        o.addOption(ACTCOUNT_KEY, true,
                "Flexibility constraints parameter: the number of activations per year");
        o.addOption(DISTINDEX_KEY, true,
                "The index of distibution file to load. Make sure the input file is named using "
                        + "following convention: "
                        + AbstractWgmfGameRunner.WIND_DISTRIBUTIONFILE_TEMPLATE
                        + " with * replaced by \"[dIxd]\" or \"\" if dIdx < 0.");
        o.addOption(PROFINDEX_KEY, true,
                "The index of elec data profile file to load. Make sure the input file is named "
                        + "using "
                        + "following convention: "
                        + AbstractWgmfGameRunner.DATAPROFILE_TEMPLATE
                        + " with * replaced by \"[dIxd]\" or \"\" if dIdx < 0.");
        o.addOption(CACHING_ALLOC_KEY, true, "Caching enabled");

        o.addOption(withLongOpt("errorDistribution")
                .withDescription("Which forecast error distribution to to use. [NORMAL|CAUCHY]")
                .hasArg()
                .withArgName("DISTRIBUTION")
                .create(DISTRIBUTION_KEY));

        int nAgents = NAGENTS_DEFAULT;
        int nReps = NREPS_DEFAULT;
        Solvers.TYPE solver = SOLVER_DEFAULT;
        boolean remoteExec = false;
        ExperimentParams.Builder builder = ExperimentParams.builder();
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(o, args);
            if (line.hasOption(AGENT_KEY)) {
                nAgents = Integer.parseInt(line.getOptionValue(AGENT_KEY));
            }
            if (line.hasOption(REPS_KEY)) {
                nReps = Integer.parseInt(line.getOptionValue(REPS_KEY));
            }
            if (line.hasOption(SOLVER_KEY)) {
                solver = Solvers.TYPE.valueOf(line.getOptionValue(SOLVER_KEY));
            }
            if (line.hasOption(MODE_KEY) && "REMOTE".equals(line.getOptionValue(MODE_KEY))) {
                remoteExec = true;
            }
            if (line.hasOption(P1START_KEY)) {
                builder.setP1Start(Double.parseDouble(line.getOptionValue(P1START_KEY)));
            }
            if (line.hasOption(P1STEP_KEY)) {
                builder.setP1Step(Double.parseDouble(line.getOptionValue(P1STEP_KEY)));
            }
            if (line.hasOption(P1END_KEY)) {
                builder.setP1End(Double.parseDouble(line.getOptionValue(P1END_KEY)));
            }
            if (line.hasOption(DISTINDEX_KEY)) {
                builder.setWindErrorProfileIndex(
                        Integer.parseInt(line.getOptionValue(DISTINDEX_KEY)));
            }
            if (line.hasOption(PROFINDEX_KEY)) {
                builder.setCurrentDataProfileIndex(
                        Integer.parseInt(line.getOptionValue(PROFINDEX_KEY)));
            }
            if (line.hasOption(CACHING_ALLOC_KEY)) {
                builder.setCachingEnabled(true);
                boolean upd = line.getOptionValue(CACHING_ALLOC_KEY).contains("u");
                boolean ensure = line.getOptionValue(CACHING_ALLOC_KEY).contains("e");
                builder.setUpdateCacheEnabled(upd);
                builder.setCacheExistenceEnsured(ensure);
            }
            if (line.hasOption(DISTRIBUTION_KEY)) {
                builder.setDistribution(
                        ErrorDistributionType.from(line.getOptionValue(DISTRIBUTION_KEY)));
            }//TODO refactor out logic specific stuff to game params.
            if (line.hasOption(INTERARRIVAL_KEY) && line.hasOption(DURATION_KEY) && line
                    .hasOption(ACTCOUNT_KEY)) {
                double ia = Double.parseDouble(line.getOptionValue(INTERARRIVAL_KEY));
                double duration = Double.parseDouble(line.getOptionValue(DURATION_KEY));
                int actCount = Integer.parseInt(line.getOptionValue(ACTCOUNT_KEY));
                builder.setActivationConstraints(HourlyFlexConstraints.builder()
                        .interActivationTime(ia).activationDuration(duration)
                        .maximumActivations(actCount).build());
            } else if ((line.hasOption(INTERARRIVAL_KEY) || line.hasOption(DURATION_KEY) || line
                    .hasOption(ACTCOUNT_KEY))) {
                throw new IllegalArgumentException(
                        "You should specify all activation constraints parameters or none at all,"
                                + " which sets the default R3DP constraints.");
            } else {
                builder.setActivationConstraints(HourlyFlexConstraints.R3DP);
            }

            if (logger.isWarnEnabled()) {
                String remote = remoteExec ? "REMOTE" : "LOCAL";
                logger.warn(
                        "Performing {} repititions for experiment with {} agents using: {} "
                                + "execution mode: {}",
                        nReps, nAgents, solver.toString(), remote);
            }
            return builder.setNAgents(nAgents).setNRepititions(nReps)
                    .setSolver(solver).setRemoteExecutable(remoteExec).build();

        } catch (ParseException exp) {
            // oops, something went wrong
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("WgmfGameRunner", o);
            if (logger.isErrorEnabled()) {
                logger.error("Parsing failed.  Reason: {}", exp.getMessage(), exp);
            }
            throw new IllegalStateException(exp);
        }
    }
}

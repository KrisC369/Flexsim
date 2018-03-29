package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.gametheory.evolutionary.EvolutionaryGameDynamics;
import be.kuleuven.cs.gametheory.results.GameResult;
import be.kuleuven.cs.gametheory.results.HeuristicSymmetricPayoffMatrix;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * A representation of a full game specification for all configurations of
 * agents over the action space.
 *
 * @param <T> The type instances for games returned.
 * @param <I> The type of results expected to be handled.
 * @param <R> The main result type to aggregate and return.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public abstract class AbstractGame<T, I, R> {
    private static final String CONSOLE = "CONSOLE";
    private static final String CONFIGURING = "Configuring instance: ";
    private static final String EXECUTING = "Executing instance: ";
    private final HeuristicSymmetricPayoffMatrix payoffs;
    private final Logger logger;
    private final List<T> instanceList;

    /**
     * Default constructor.
     *
     * @param agents  The number of agents.
     * @param actions The number of actions that agents can choose from.
     */
    public AbstractGame(final int agents, final int actions) {
        this.payoffs = new HeuristicSymmetricPayoffMatrix(agents, actions);
        this.logger = LoggerFactory.getLogger(CONSOLE);
        this.instanceList = Lists.newArrayList();
    }

    /**
     * Configure and set-up the game specifics for different iterations of games
     * over the strategy space.
     */
    protected abstract void configureInstances();

    /**
     * Run experiments.
     */
    protected abstract void runExperiments();

    /**
     * @return The runnable game instances.
     */
    public List<T> getGameInstances() {
        return Collections.unmodifiableList(this.instanceList);
    }

    protected List<T> getInternalInstanceList() {
        return this.instanceList;
    }

    /**
     * @param results The simulation results to parse and process.
     */
    public abstract void gatherResults(List<I> results);

    protected final void addPayoffEntry(final Double[] values, final int[] entry) {
        this.payoffs.addEntry(values, entry);
    }

    protected void printConfigProgress(final int progressCounter, final long l) {
        printProgress(progressCounter, l, CONFIGURING);

    }

    protected void printExecutionProgress(final int progressCounter, final long l) {
        printProgress(progressCounter, l, EXECUTING);
    }

    protected void printProgress(final int progressCounter, final long l, final String message) {
        final StringBuilder b = new StringBuilder();
        b.append(message).append(progressCounter).append("/").append(l);
        logger.info(b.toString());
    }

    protected void logResults() {
        final StringBuilder b = new StringBuilder(30);
        b.append(getResultString()).append("\n")
                .append("Dynamics equation params:");
        for (final Double d : EvolutionaryGameDynamics.from(payoffs).getDynamicEquationFactors()) {
            b.append(d).append("\n");
        }
        logger.debug(b.toString());
    }

    protected String getResultString() {
        return payoffs.toString();
    }

    /**
     * Returns the parameters of the dynamics in a formatted string.
     *
     * @return the params in a MATLAB style formatted string.
     */
    protected String getDynamicsParametersString() {
        final StringBuilder b = new StringBuilder(30);
        char character = 'a';
        b.append("\n");
        for (final Double d : EvolutionaryGameDynamics.from(payoffs)
                .getDynamicEquationFactors()) {
            b.append(character++).append("=").append(d).append(";\n");
        }
        return b.toString();
    }

    /**
     * Constructs the results from the current game.
     *
     * @return A gameresult object based on the currently available result date
     * for this game.
     */
    protected abstract GameResult<R> getResults();

    protected HeuristicSymmetricPayoffMatrix getPayoffs() {
        return this.payoffs;
    }
}

package be.kuleuven.cs.gametheory;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;

import com.google.common.collect.Maps;

/**
 * This class represents heuristic payoff tables or matrices. The heuristic part
 * stems from the experimentally gathered values in this table. This table is
 * meant to be filled in with experimentation results. A table consists out of
 * entries for every combination of agents over the action space.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class HeuristicPayoffMatrix {
    private final int agents;
    private final int actions;
    private final Map<PayoffEntry, Long> table;
    private final Map<PayoffEntry, Integer> tableCount;
    private final long numberOfCombinations;

    /**
     * Default constructor using the dimensions of the table.
     * 
     * @param agents
     *            the amount of agents.
     * @param actions
     *            the amount of actions.
     */
    public HeuristicPayoffMatrix(int agents, int actions) {
        this.agents = agents;
        this.actions = actions;
        this.table = Maps.newLinkedHashMap();
        this.tableCount = Maps.newLinkedHashMap();
        this.numberOfCombinations = MathUtils.multiCombinationSize(actions,
                agents);
    }

    /**
     * Returns true if every space in the table is filled in with a value.
     * 
     * @return true if every entry has a value.
     */
    public boolean isComplete() {
        long possibilities = getNumberOfPossibilities();
        if (this.table.size() != possibilities)
            return false;
        return true;
    }

    private long getNumberOfPossibilities() {
        return numberOfCombinations;
    }

    /**
     * Adds a new entry to this payoff matrix.
     * 
     * @param value
     *            The payoff value.
     * @param key
     *            The population shares as indeces for the value
     */
    public void addEntry(int value, int... key) {
        checkArgument(testKey(key));
        PayoffEntry entry = PayoffEntry.from(key);
        if (getEntryCount(entry) == 0) {
            newEntry(entry, value);

        } else {
            plusEntry(entry, value);
        }
    }

    private void plusEntry(PayoffEntry entry, int value) {
        this.table.put(entry, table.get(entry) + value);
        this.tableCount.put(entry, tableCount.get(entry) + 1);
    }

    private void newEntry(PayoffEntry entry, int value) {
        this.table.put(entry, (long) value);
        this.tableCount.put(entry, 1);
    }

    private int getEntryCount(PayoffEntry entry) {
        if (this.tableCount.containsKey(entry)) {
            return this.tableCount.get(entry);
        }
        return 0;
    }

    private boolean testKey(int[] key) {
        if (key.length != actions) {
            return false;
        }
        int count = 0;
        for (int i : key) {
            count += i;
        }
        if (count != agents) {
            return false;
        }
        return true;
    }

    /**
     * Returns an entry in the payoff matrix.
     * 
     * @param key
     *            the index keys.
     * @return the value recorded in the matrix.
     */
    public double getEntry(int... key) {
        checkArgument(testKey(key));
        PayoffEntry entry = PayoffEntry.from(key);
        checkArgument(tableCount.containsKey(entry));
        return (table.get(entry) / tableCount.get(entry));
    }
}

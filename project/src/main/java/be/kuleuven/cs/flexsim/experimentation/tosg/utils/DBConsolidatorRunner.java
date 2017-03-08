package be.kuleuven.cs.flexsim.experimentation.tosg.utils;

import be.kuleuven.cs.flexsim.persistence.MapDBConsolidator;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Small utility for consolidating databases.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class DBConsolidatorRunner {

    public static void main(String[] args) {
        String outFile = "consolidation/memoDB_R400N8P0E0.db";
        List<String> files = Lists.newArrayList("consolidation/memo2.db",
                "consolidation/memo4.db", "consolidation/memo5.db",
                "consolidation/memo6.db", "consolidation/memo7.db", "consolidation/memo8.db",
                "consolidation/memo9.db", "consolidation/memo10.db");
        MapDBConsolidator<ImmutableSolverProblemContextView, AllocResultsView> cons = new
                MapDBConsolidator(files, outFile);
        cons.consolidate();
    }
}

package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.experimentation.tosg.adapters.SolutionResultAdapter;
import be.kuleuven.cs.flexsim.experimentation.tosg.adapters.SolverAdapter;
import be.kuleuven.cs.flexsim.solvers.AllocResults;
import be.kuleuven.cs.flexsim.solvers.Solvers;

import java.io.Serializable;
import java.util.Collection;

/**
 * A solvers factory for wgmf games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *///TODO move to outer package
public class WgmfSolverFactory implements AbstractSolverFactory<SolutionResults>, Serializable {
    private static final long serialVersionUID = -5851172788369007725L;
    private final Solvers.TYPE type;
    private final String filepath;
    private final String dbWriteFileLocation;
    private final boolean cachingEnabled;
    private final boolean updateCache;
    private final boolean ensureCacheFileExists;
    private long defaultSeed = 0L;

    WgmfSolverFactory(Solvers.TYPE type, String filepath, String dbWriteFileLocation,
            boolean cachingEnabled, boolean updateCache, boolean ensureCacheFileExists) {
        this.type = type;
        this.filepath = filepath;
        this.dbWriteFileLocation = dbWriteFileLocation;
        this.cachingEnabled = cachingEnabled;
        this.updateCache = updateCache;
        this.ensureCacheFileExists = ensureCacheFileExists;
    }

    public void setSeed(long seed) {
        this.defaultSeed = seed;
    }

    @Override
    public Solver<SolutionResults> createSolver(final FlexAllocProblemContext context) {
        final FlexAllocProblemContext flexAllocProblemContext = new FlexAllocProblemContext() {
            @Override
            public Collection<FlexibilityProvider> getProviders() {
                return context.getProviders();
            }

            @Override
            public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                return context.getEnergyProfileToMinimizeWithFlex();
            }

            @Override
            public long getSeedValue() {
                return defaultSeed;
            }
        };
        final Solver<AllocResults> solverInstance;
        if (cachingEnabled) {
            solverInstance = type
                    .getCachingInstance(flexAllocProblemContext, filepath, dbWriteFileLocation,
                            updateCache, ensureCacheFileExists);
        } else {
            solverInstance = type.getInstance(flexAllocProblemContext);
        }
        return new SolverAdapter<AllocResults, SolutionResults>(solverInstance) {

            @Override
            public SolutionResults adaptResult(AllocResults solution) {
                return new SolutionResultAdapter(solution).getResults();
            }
        };
    }
}

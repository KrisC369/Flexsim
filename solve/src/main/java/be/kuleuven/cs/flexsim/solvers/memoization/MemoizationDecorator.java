package be.kuleuven.cs.flexsim.solvers.memoization;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.persistence.MemoizationContext;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import be.kuleuven.cs.flexsim.solvers.optimal.AllocResults;

/**
 * Decorator making use of memozation to avoid costly recalculation.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MemoizationDecorator implements Solver<AllocResults> {
    private final Solver<AllocResults> actualSolver;
    private FlexAllocProblemContext context;
    private final MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>
            memoization;
    private final ImmutableSolverProblemContextView contextView;

    /**
     * Constructor
     *
     * @param actualSolver
     * @param context
     * @param memoization
     */
    public MemoizationDecorator(Solver<AllocResults> actualSolver, FlexAllocProblemContext context,
            MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView> memoization) {
        this.actualSolver = actualSolver;
        this.context = context;
        this.memoization = memoization;
        this.contextView = ImmutableSolverProblemContextView.from(context);
    }

    @Override
    public AllocResults solve() {
        return memoization.testAndCall(contextView, this::calculateResult).toBackedView(context);
    }

    private AllocResultsView calculateResult() {
        return AllocResultsView.from(actualSolver.solve());
    }
}

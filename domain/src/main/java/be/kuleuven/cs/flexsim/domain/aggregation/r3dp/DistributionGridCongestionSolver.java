package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.util.Payment;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ListMultimap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

/**
 * Represents a distribution grid management entity that solves current congestion problems in
 * distribution grid infrastructure.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DistributionGridCongestionSolver extends FlexibilityUtiliser<SolutionResults> {
    private static final double FIXED_PRICE = 35.4;
    protected static final double TO_KILO = 10E3d;
    private final CongestionProfile congestion;

    @Nullable
    private SolutionResults results;

    /**
     * Default constructor
     *
     * @param fac The factory that provides the solver.
     * @param c   The congestion profile
     */
    public DistributionGridCongestionSolver(final AbstractSolverFactory<SolutionResults> fac,
            final CongestionProfile c) {
        super(fac);
        congestion = c;
    }

    @Override
    protected void performSolveStep() {
        Solver<SolutionResults> solver = getSolverFactory()
                .createSolver(new FlexAllocProblemContext() {
                    @Override
                    public Iterable<FlexibilityProvider> getProviders() {
                        return Collections.unmodifiableSet(getFlexibilityProviders());
                    }

                    @Override
                    public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                        return congestion;
                    }
                });
        solver.solve();
        this.results = solver.getSolution();
        processActivations(results);
    }

    private void processActivations(@Nullable final SolutionResults results) {
        List<Integer> acts = consolidateActivations(results.getAllocationMaps());
        for (final FlexibilityProvider p : getFlexibilityProviders()) {
            processActivationsFor(p, results.getAllocationMaps().get(p),
                    results.getDiscretisationInNbSlotsPerHour(), acts);
        }
    }

    @VisibleForTesting
    List<Integer> consolidateActivations(
            ListMultimap<FlexibilityProvider, Boolean> values) {
        OptionalInt min = values.keySet().stream().mapToInt(fpC -> values.get(fpC).size())
                .reduce(Integer::min);
        IntList toRet = new IntArrayList(
                min.orElseThrow(() -> new IllegalStateException("No int found.")));
        for (int j = 0;
             j < min.orElseThrow(() -> new IllegalStateException("No int found.")); j++) {
            final int jj = j;
            toRet.add(
                    values.keySet().stream().mapToInt(fp -> values.get(fp).get(jj) ? 1 : 0).sum());
        }
        return toRet;
    }

    private void processActivationsFor(final FlexibilityProvider p,
            final List<Boolean> booleen, final int discretisationInNbSlotsPerHour,
            List<Integer> acts) {
        int ind = 0;
        while (ind < booleen.size()) {
            if (booleen.get(ind)) {
                //start activation.
                final int start = ind;
                int count = 0;
                while (ind < booleen.size() && booleen.get(ind)) {
                    count++;
                    ind++;
                }
                FlexActivation activation = FlexActivation
                        .create(start / (double) discretisationInNbSlotsPerHour,
                                count / (double) discretisationInNbSlotsPerHour,
                                count * p.getFlexibilityActivationRate().getUp()
                                        / (double) discretisationInNbSlotsPerHour);
                p.registerActivation(activation, Payment.create(
                        calculatePaymentFor(activation, discretisationInNbSlotsPerHour, acts)));
            }
            ind++;
        }
    }

    protected double calculatePaymentFor(FlexActivation activation,
            int discretisationInNbSlotsPerHour, List<Integer> acts) {
        return activation.getEnergyVolume() * (FIXED_PRICE / TO_KILO);
    }

    @Override
    protected SolutionResults getResult() {
        return results;
    }
}

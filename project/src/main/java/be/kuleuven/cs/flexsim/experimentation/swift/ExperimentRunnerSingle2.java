/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import autovalue.shaded.com.google.common.common.collect.Lists;
import be.kuleuven.cs.flexsim.domain.energy.dso.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CompetitiveCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CooperativeCongestionSolver;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentCallback;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.runners.local.SingleThreadedExperimentRunner;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunnerSingle2 {

    private static int N = 10000;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final int NAGENTS = 10;
    private static final int ALLOWED_EXCESS = 50;
    private final List<Double> result1 = Lists.newArrayList();
    private final List<Double> result2 = Lists.newArrayList();
    private boolean competitive = false;
    private boolean allowLessActivations = true;

    /**
     * @param args
     */
    public static void main(String[] args) {
        ExperimentRunnerSingle2 er = new ExperimentRunnerSingle2();
        // er.runBatch();
        er.runSingle();
    }

    /**
     * 
     */
    protected void runSingle() {
        CongestionProfile profile;
        double[] result = new double[100];
        try {
            profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
            GammaDistribution gd = new GammaDistribution(
                    new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                    R3DP_GAMMA_SCALE);
            for (int i = 0; i < N; i++) {
                ExperimentInstance p = (new ExperimentInstance(NAGENTS,
                        getSolverBuilder(ALLOWED_EXCESS), gd.sample(NAGENTS),
                        profile, allowLessActivations));
                p.startExperiment();
                result[i / 100] += p.getEfficiency();
                // System.out.println(p.getEfficiency());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < N / 100; i++) {
            result[i] /= 100.0;
        }
        System.out.println("distribution of eff = " + Arrays.toString(result));
    }

    private SolverBuilder getSolverBuilder(int i) {
        if (competitive) {
            return new CompetitiveSolverBuilder();
        }
        return new CooperativeSolverBuilder(i);
    }

    private String getLabel() {
        if (competitive) {
            return "comp";
        }
        return "coop";
    }

    public void runBatch() {
        CongestionProfile profile;
        List<ExperimentAtom> instances = Lists.newArrayList();
        GammaDistribution gd = new GammaDistribution(
                new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                R3DP_GAMMA_SCALE);
        try {
            profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
            for (int i = 0; i < N; i++) {
                instances.add(new ExperimentAtomImplementation(gd.sample(10),
                        profile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // ExperimentRunner r = new MultiThreadedExperimentRunner(8);
        ExperimentRunner r = new SingleThreadedExperimentRunner();

        r.runExperiments(instances);
        System.out.println(result1);
    }

    private synchronized void addResult(String lable, double eff) {
        if ("comp".equals(lable)) {
            result1.add(eff);
        } else if ("coop".equals(lable)) {
            result2.add(eff);
        }
    }

    class ExperimentAtomImplementation extends ExperimentAtomImpl {
        private @Nullable double[] real;
        private @Nullable ExperimentInstance p;
        private @Nullable CongestionProfile profile;

        ExperimentAtomImplementation(double[] realisation,
                CongestionProfile profile) {
            this.real = realisation;
            this.profile = profile;
            this.registerCallbackOnFinish(new ExperimentCallback() {

                @Override
                public void callback(ExperimentAtom instance) {
                    addResult(getLabel(), checkNotNull(p).getEfficiency());
                    p = null;
                }
            });
        }

        private void start() {
            checkNotNull(p);
            p.startExperiment();
        }

        private void setup() {
            this.p = (new ExperimentInstance(NAGENTS,
                    getSolverBuilder(ALLOWED_EXCESS), checkNotNull(real),
                    checkNotNull(profile), allowLessActivations));
        }

        @Override
        protected void execute() {
            setup();
            start();
        }
    }

    class CompetitiveSolverBuilder implements SolverBuilder {
        @Override
        public AbstractCongestionSolver getSolver(CongestionProfile profile,
                int n) {
            return new CompetitiveCongestionSolver(profile, 8, ALLOWED_EXCESS);
        }
    }

    class CooperativeSolverBuilder implements SolverBuilder {
        int i;

        public CooperativeSolverBuilder(int i) {
            this.i = i;
        }

        @Override
        public AbstractCongestionSolver getSolver(CongestionProfile profile,
                int n) {
            return new CooperativeCongestionSolver(profile, 8, i);
        }
    }
}

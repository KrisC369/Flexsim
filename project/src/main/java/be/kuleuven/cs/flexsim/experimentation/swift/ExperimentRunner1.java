/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentCallback;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunner1 extends ExperimentRunnerAllRes {

    private static final int N = 1000;
    private final int n;
    private final int nagents;

    private boolean allowLessActivations = true;

    protected ExperimentRunner1(int N, int nagents, int allowed) {
        super(N, nagents, allowed);
        this.n = N;
        this.nagents = nagents;
    }

    /**
     * @param args
     *            Standard in args.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            new ExperimentRunner1(10, 3, 33).execute();
        } else if (args.length == 1) {
            try {
                final int agents = Integer.valueOf(args[0]);
                new ExperimentRunner1(N, agents, 33).execute();
            } catch (Exception e) {
                LoggerFactory.getLogger(ExperimentRunner1.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        } else if (args.length == 2) {
            try {
                final int agents = Integer.valueOf(args[1]);
                final int reps = Integer.valueOf(args[0]);
                new ExperimentRunner1(reps, agents, 33).execute();
            } catch (Exception e) {
                LoggerFactory.getLogger(ExperimentRunner1.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        } else if (args.length == 3) {
            try {
                final int agents = Integer.valueOf(args[1]);
                final int reps = Integer.valueOf(args[0]);
                final int allowed = Integer.valueOf(args[2]);
                new ExperimentRunner1(reps, agents, allowed).execute();
            } catch (Exception e) {
                LoggerFactory.getLogger(ExperimentRunner1.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        }
    }

    @Override
    protected void printResult() {
        System.out.println("BEGINRESULT:");
        System.out.println("Res1=" + getMainRes1());
        System.out.println("Res2=" + getMainRes2());
        System.out.println("Not meeting 40 acts: "
                + String.valueOf(n - getMainRes1().size()));
        System.out.println("Not meeting 40 acts: "
                + String.valueOf(n - getMainRes2().size()));
        System.out.println("ENDRESULT:");
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
                    addMainResult(getLabel(), checkNotNull(p).getEfficiency());
                    p = null;
                }
            });
        }

        private void start() {
            checkNotNull(p);
            p.startExperiment();
        }

        private void setup() {
            this.p = (new ExperimentInstance(nagents, getSolverBuilder(),
                    checkNotNull(real), checkNotNull(profile),
                    allowLessActivations));
        }

        @Override
        protected void execute() {
            setup();
            start();
        }
    }
}

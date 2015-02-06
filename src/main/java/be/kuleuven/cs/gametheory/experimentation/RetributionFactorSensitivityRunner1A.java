package be.kuleuven.cs.gametheory.experimentation;

import be.kuleuven.cs.gametheory.experimentation.runners.MultiThreadedExperimentRunner;

/**
 * An example class running some experiments.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class RetributionFactorSensitivityRunner1A extends
        RetributionFactorSensitivityRunner {

    private static final int NAGENTS = 1;
    private static final int REPITITIONS = 400;
    private static final int THREADS = 3;
    private static final String TAG = "RESULT1A";

    protected RetributionFactorSensitivityRunner1A() {
        super(REPITITIONS, NAGENTS, TAG);
    }

    /**
     * Runs some experiments as a PoC.
     * 
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        new MultiThreadedExperimentRunner(
                new RetributionFactorSensitivityRunner1A(), THREADS)
                .runExperiments();
    }
}

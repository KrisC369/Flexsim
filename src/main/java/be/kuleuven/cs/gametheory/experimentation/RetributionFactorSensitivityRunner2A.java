package be.kuleuven.cs.gametheory.experimentation;

import be.kuleuven.cs.gametheory.experimentation.runners.MultiThreadedExperimentRunner;

/**
 * An example class running some experiments.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class RetributionFactorSensitivityRunner2A extends
        RetributionFactorSensitivityRunner {

    private static final int NAGENTS = 2;
    private static final int REPITITIONS = 200;
    private static final int THREADS = 3;
    private static final String TAG = "RESULT2A";

    protected RetributionFactorSensitivityRunner2A() {
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
                new RetributionFactorSensitivityRunner2A(), THREADS)
                .runExperiments();
    }
}

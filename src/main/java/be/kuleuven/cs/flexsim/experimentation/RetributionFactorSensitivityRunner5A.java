package be.kuleuven.cs.flexsim.experimentation;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class RetributionFactorSensitivityRunner5A extends
        RetributionFactorSensitivityRunner {

    private static final int NAGENTS = 5;
    private static final int REPITITIONS = 15;
    private static final String TAG = "RESULT5A";

    protected RetributionFactorSensitivityRunner5A() {
        super(REPITITIONS, NAGENTS, TAG);
    }

    /**
     * Runs some experiments as a PoC.
     * 
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        new RetributionFactorSensitivityRunner5A().execute();
    }
}

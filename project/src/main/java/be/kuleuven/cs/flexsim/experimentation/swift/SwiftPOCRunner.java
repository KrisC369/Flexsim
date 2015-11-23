/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.annotation.Nullable;

import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.energy.dso.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CompetitiveCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CooperativeCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.DSMPartner;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class SwiftPOCRunner {

    private static final int POWERRATE = 618;
    private static final int SIMDURATION = 4 * 24 * 365;
    private static final int TIMEHORIZON = 8;
    private static final boolean COMPETITIVE = true;

    private @Nullable CongestionProfile profile;
    private final AbstractCongestionSolver solver;
    private final DSMPartner partner1;
    private final DSMPartner partner2;
    private final Simulator sim;

    /**
     * @param args
     *            std in params.
     */
    public static void main(String[] args) {
        SwiftPOCRunner r = new SwiftPOCRunner();
        r.startExperiment();
        r.displayEfficiency();
    }

    /**
     * Default constructor.
     */
    public SwiftPOCRunner() {
        try {
            this.profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
        } catch (IOException e) {
            LoggerFactory.getLogger(SwiftPOCRunner.class)
                    .error("IOException while opening profile.", e);
        }
        if (COMPETITIVE) {
            this.solver = new CompetitiveCongestionSolver(checkNotNull(profile),
                    TIMEHORIZON);
        } else {
            this.solver = new CooperativeCongestionSolver(checkNotNull(profile),
                    TIMEHORIZON);
        }
        this.partner1 = new DSMPartner(POWERRATE);
        this.partner2 = new DSMPartner(POWERRATE / 2);
        solver.registerDSMPartner(partner1);
        solver.registerDSMPartner(partner2);
        sim = Simulator.createSimulator(SIMDURATION);
        sim.register(solver);
    }

    /**
     * Start this proof of concept experiment.
     */
    public void startExperiment() {
        this.sim.start();
    }

    private void displayEfficiency() {
        double eff = solver.getTotalRemediedCongestion()
                / ((40.0 * 2.0 * POWERRATE) + (40.0 * 2.0 * POWERRATE / 2));
        System.out.println("Efficiency is " + eff);
    }
}

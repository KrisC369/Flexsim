package be.kuleuven.cs.flexsim.domain.energy.dso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;

@RunWith(MockitoJUnitRunner.class)
public class DSOIntegrationTest {
    private static String column = "test";
    private static String file = "test.csv";
    private AbstractCongestionSolver congestionSolver;

    private CongestionProfile congestionProfile;

    private DSMPartner dsm1;
    private DSMPartner dsm2;

    private Simulator sim;

    @Before
    public void setUp() throws Exception {
        try {
            congestionProfile = (CongestionProfile) CongestionProfile
                    .createFromCSV(file, column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CooperativeCongestionSolver(congestionProfile,
                8);
        dsm1 = new DSMPartner(0, 48, 8, 100, 1);
        dsm2 = new DSMPartner(0, 48, 8, 50, 1);
        // when(congestionProfile.value(anyInt())).thenReturn(175.0);
        // when(congestionProfile.values()).thenReturn(new double[4 * 24 *
        // 365]);

        sim = Simulator.createSimulator(500 - 1);
        sim.register(congestionSolver);
    }

    @Test
    public void testNoActivation() {
        congestionSolver = new CooperativeCongestionSolver(congestionProfile,
                8);
        dsm1 = new DSMPartner(0, 48, 8, 100, 1);
        dsm2 = new DSMPartner(0, 48, 8, 50, 1);
        register();
        sim.register(congestionSolver);
        sim.start();
        assertEquals(0, congestionSolver.getTotalRemediedCongestion(), 0);

    }

    private void register() {
        congestionSolver.registerDSMPartner(dsm1);
        congestionSolver.registerDSMPartner(dsm2);
    }

    @Test
    public void testPosActivation() {
        congestionSolver = new CooperativeCongestionSolver(congestionProfile, 8,
                100);
        dsm1 = new DSMPartner(40, 48, 8, 100, 1);
        dsm2 = new DSMPartner(40, 48, 8, 50, 1);
        register();
        sim.register(congestionSolver);
        sim.start();
        // assertNotEquals(0.0, congestionSolver.getTotalRemediedCongestion());
        assertEquals(DSMPartner.R3DPMAX_ACTIVATIONS,
                dsm1.getCurrentActivations(),
                DSMPartner.R3DPMAX_ACTIVATIONS - 5);
        assertEquals(DSMPartner.R3DPMAX_ACTIVATIONS,
                dsm2.getCurrentActivations(),
                DSMPartner.R3DPMAX_ACTIVATIONS - 5);
    }

    @Test
    public void testPosActivationNumberActivations() {
        int power = 100;
        congestionSolver = new CooperativeCongestionSolver(congestionProfile, 8,
                100);
        dsm1 = new DSMPartner(40, 48, 8, power, 1);
        register();
        sim.register(congestionSolver);
        sim.start();
        // assertNotEquals(0.0, congestionSolver.getTotalRemediedCongestion());
        assertEquals(DSMPartner.R3DPMAX_ACTIVATIONS,
                dsm1.getCurrentActivations(),
                DSMPartner.R3DPMAX_ACTIVATIONS - 5);
        assertTrue(congestionSolver.getTotalRemediedCongestion() <= power
                * DSMPartner.R3DPMAX_ACTIVATIONS * 2);
    }

}

package be.kuleuven.cs.gridflex.domain.energy.dso.contractnet;

import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.simulation.Simulator;
import com.google.common.collect.Lists;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class DSOIntegrationTest {
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;

    private static String column = "test";
    private static String file = "test.csv";
    private AbstractCongestionSolver congestionSolver;

    private CongestionProfile abstractTimeSeriesImplementation;

    private DSMPartner dsm1;
    private DSMPartner dsm2;

    private Simulator sim;

    @Before
    public void setUp() throws Exception {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV(file, column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation, 8,
                5);
        dsm1 = new DSMPartner(0, 48, 8, 100, 1);
        dsm2 = new DSMPartner(0, 48, 8, 50, 1);
        // when(abstractTimeSeriesImplementation.value(anyInt())).thenReturn(175.0);
        // when(abstractTimeSeriesImplementation.values()).thenReturn(new double[4 * 24 *
        // 365]);

        sim = Simulator.createSimulator(600 - 1);
        // sim.register(congestionSolver);
    }

    @Test
    public void testNoActivation() {
        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation, 8,
                100);
        dsm1 = new DSMPartner(0, 48, 8, 100, 1);
        dsm2 = new DSMPartner(0, 48, 8, 50, 1);
        register();
        sim.start();
        assertEquals(0, congestionSolver.getTotalRemediedCongestion(), 0);

    }

    private void register() {
        congestionSolver.registerDSMPartner(dsm1);
        congestionSolver.registerDSMPartner(dsm2);
        sim.register(congestionSolver);
    }

    @Test
    public void testPosActivation() {
        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation, 8,
                5);
        dsm1 = new DSMPartner(40, 48, 8, 100, 1);
        dsm2 = new DSMPartner(40, 48, 8, 50, 1);
        register();
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
    public void testRemediedCongestion() {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 15000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 8000, 1);
        sim = Simulator.createSimulator(25);
        register();
        sim.start();
        double sum = 0;
        for (double d : congestionSolver.getProfileAfterDSM().values()) {
            if (d > 0) {
                sum += d;
            }
        }
        double remedied = congestionSolver.getCongestion().sum() - sum;
        assertEquals(remedied, congestionSolver.getTotalRemediedCongestion(),
                0.1);
        assertNotEquals(congestionSolver.getCongestion().sum(),
                congestionSolver.getProfileAfterDSM().sum());
    }

    @Test
    public void testPseudoRandomGen() {
        int N = 200;
        GammaDistribution gd = new GammaDistribution(
                new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                R3DP_GAMMA_SCALE);
        double[] realisation1 = gd.sample(N);
        gd = new GammaDistribution(new MersenneTwister(1312421l),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        double[] realisation2 = gd.sample(N);
        assertEquals(Arrays.toString(realisation1),
                Arrays.toString(realisation2));
    }

    @Test
    public void testPosActivationNumberActivations() {
        int power = 100;
        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation, 8,
                5);
        dsm1 = new DSMPartner(40, 48, 8, power, 1);
        register();
        sim.start();
        // assertNotEquals(0.0, congestionSolver.getTotalRemediedCongestion());
        assertEquals(DSMPartner.R3DPMAX_ACTIVATIONS,
                dsm1.getCurrentActivations(),
                DSMPartner.R3DPMAX_ACTIVATIONS - 5);
        assertTrue(congestionSolver.getTotalRemediedCongestion() <= power
                * DSMPartner.R3DPMAX_ACTIVATIONS * 2);
    }

    @Test
    public void testCoopScenario1() {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 2000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 500, 1);
        sim = Simulator.createSimulator(25);
        register();
        sim.start();
        System.out.println();

    }

    @Test
    public void testCoopScenario2() {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 15000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 8000, 1);
        sim = Simulator.createSimulator(25);
        register();
        // sim.start();

        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurrentActivations() == 0);
    }

    @Test
    public void testCompScenario1() {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CompetitiveCongestionSolver(abstractTimeSeriesImplementation,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 15000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 8000, 1);
        sim = Simulator.createSimulator(25);
        register();
        // sim.start();

        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() == 0);
        assertTrue(dsm1.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurtailment(10) == dsm2.getFlexPowerRate());
        assertEquals(0.01, getEfficiency(), 0.1);
    }

    public double getEfficiency() {
        return congestionSolver.getTotalRemediedCongestion()
                / (getTotalPowerRates() * dsm1.getMaxActivations() * 2.0);
    }

    private double getTotalPowerRates() {
        int sum = 0;
        sum += dsm1.getFlexPowerRate();
        sum += dsm2.getFlexPowerRate();
        return sum;
    }

    @Test
    public void testScenarioSquareProfileEqualReduction() {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CompetitiveCongestionSolver(abstractTimeSeriesImplementation,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 16000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 8000, 1);
        sim = Simulator.createSimulator(25);
        register();
        // sim.start();

        for (int i = 0; i < 4; i++) {
            congestionSolver.afterTick(1);
        }
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() == 0);
        assertTrue(dsm1.getCurrentActivations() > 0);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurtailment(10) == dsm2.getFlexPowerRate());
        for (int i = 0; i < 8; i++) {
            congestionSolver.afterTick(1);
        }
        double eff1 = congestionSolver.getTotalRemediedCongestion();

        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 16000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 8000, 1);
        sim = Simulator.createSimulator(25);
        register();
        // sim.start();

        for (int i = 0; i < 4; i++) {
            congestionSolver.afterTick(1);
        }
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurrentActivations() == 0);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurtailment(10) == dsm1.getFlexPowerRate());
        for (int i = 0; i < 8; i++) {
            congestionSolver.afterTick(1);
        }
        double eff2 = congestionSolver.getTotalRemediedCongestion();
        assertEquals(eff1, eff2, 0.001);
    }

    @Test
    public void testScenarioVaryingProfileUnequalReduction() {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        abstractTimeSeriesImplementation.changeValue(6, 1500);
        abstractTimeSeriesImplementation.changeValue(7, 1300);
        abstractTimeSeriesImplementation.changeValue(8, 9000);
        abstractTimeSeriesImplementation.changeValue(9, 12000);
        abstractTimeSeriesImplementation.changeValue(9, 2300);
        congestionSolver = new CompetitiveCongestionSolver(abstractTimeSeriesImplementation,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 16000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 4000, 1);
        sim = Simulator.createSimulator(25);
        register();
        // sim.start();

        for (int i = 0; i < 4; i++) {
            congestionSolver.afterTick(1);
        }
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() == 0);
        assertTrue(dsm1.getCurrentActivations() > 0);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurtailment(10) == dsm2.getFlexPowerRate());
        for (int i = 0; i < 8; i++) {
            congestionSolver.afterTick(1);
        }
        double eff1 = congestionSolver.getTotalRemediedCongestion();

        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 16000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 4000, 1);
        sim = Simulator.createSimulator(25);
        register();
        // sim.start();

        for (int i = 0; i < 4; i++) {
            congestionSolver.afterTick(1);
        }
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() == 0);
        assertTrue(dsm1.getCurrentActivations() > 0);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurtailment(10) == dsm1.getFlexPowerRate());
        for (int i = 0; i < 8; i++) {
            congestionSolver.afterTick(1);
        }
        double eff2 = congestionSolver.getTotalRemediedCongestion();
        assertTrue(eff1 < eff2);
    }

    @Test
    public void testCompScenario2() {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

        congestionSolver = new CompetitiveCongestionSolver(abstractTimeSeriesImplementation,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 8000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 15000, 1);
        sim = Simulator.createSimulator(25);
        register();
        // sim.start();

        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurrentActivations() == 0);
        assertTrue(dsm2.getCurtailment(10) == dsm1.getFlexPowerRate());
        assertEquals(0.01, getEfficiency(), 0.01);
    }

    @Test
    public void testScenarioManyAgents() {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV("smalltest.csv", "test2");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        int N = 200;
        abstractTimeSeriesImplementation.changeValue(6, 1500);
        abstractTimeSeriesImplementation.changeValue(7, 1300);
        abstractTimeSeriesImplementation.changeValue(8, 9000);
        abstractTimeSeriesImplementation.changeValue(9, 12000);
        abstractTimeSeriesImplementation.changeValue(9, 2300);
        congestionSolver = new CompetitiveCongestionSolver(abstractTimeSeriesImplementation, 8,
                5);
        List<DSMPartner> partners = Lists.newArrayList();
        GammaDistribution gd = new GammaDistribution(
                new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                R3DP_GAMMA_SCALE);
        for (int i = 0; i < N; i++) {
            partners.add(new DSMPartner((int) gd.sample()));
            congestionSolver.registerDSMPartner(partners.get(i));
        }
        // sim = Simulator.createSimulator(499);
        // sim.register(congestionSolver);
        // sim.start();
        for (int i = 0; i < 3; i++) {
            congestionSolver.afterTick(1);
        }
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        for (int i = 0; i < 10; i++) {
            congestionSolver.tick(1);
            congestionSolver.afterTick(1);
        }
        int totalActsComp = getTotalActs(partners);
        double eff1 = congestionSolver.getTotalRemediedCongestion();
        double eff1R = getIAgentEff(partners, 25);
        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation, 8,
                5);
        partners = Lists.newArrayList();
        gd = new GammaDistribution(new MersenneTwister(1312421l),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        for (int i = 0; i < N; i++) {
            partners.add(new DSMPartner((int) gd.sample()));
            congestionSolver.registerDSMPartner(partners.get(i));
        }
        // sim = Simulator.createSimulator(35040);
        // sim.register(congestionSolver);
        // sim.start();
        for (int i = 0; i < 3; i++) {
            congestionSolver.afterTick(1);
        }
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        for (int i = 0; i < 10; i++) {
            congestionSolver.tick(1);
            congestionSolver.afterTick(1);
        }
        double eff2 = congestionSolver.getTotalRemediedCongestion();
        double eff2R = getIAgentEff(partners, 25);
        // assertEquals(getTotalActs(partners), totalActsComp);
        System.out.println(eff1 + " " + eff2);
        assertTrue(eff1R < eff2R);
        // assertTrue(eff1 < eff2); //Allocation is lower but eff is higher.
    }

    public int getTotalActs(List<DSMPartner> partners) {
        int sum = 0;
        for (DSMPartner p : partners) {
            sum += p.getCurrentActivations();
        }
        return sum;
    }

    private double getIAgentEff(List<DSMPartner> partners, int length) {
        double eff2R = 0;
        for (DSMPartner d : partners) {
            double sum = 0;
            for (int i = 0; i <= length; i++) {
                sum += d.getCurtailment(i) / 4;
            }
            if (sum != 0) {
                eff2R += (sum / (d.getCurrentActivations()
                        * d.getFlexPowerRate() * 2));
            }
        }
        return eff2R;
    }

    @Test
    public void testScenarioManyAgents2() {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV("smalltest.csv", "test2");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CompetitiveCongestionSolver(abstractTimeSeriesImplementation, 8,
                5);
        List<DSMPartner> partners = Lists.newArrayList();
        GammaDistribution gd = new GammaDistribution(
                new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                R3DP_GAMMA_SCALE);
        for (int i = 0; i < 200; i++) {
            partners.add(new DSMPartner((int) gd.sample()));
            congestionSolver.registerDSMPartner(partners.get(i));
        }
        // sim = Simulator.createSimulator(499);
        // sim.register(congestionSolver);
        // sim.start();
        for (int i = 0; i < 3; i++) {
            congestionSolver.afterTick(1);
        }
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        for (int i = 0; i < 10; i++) {
            congestionSolver.tick(1);
            congestionSolver.afterTick(1);
        }
        int totalActsComp = getTotalActs(partners);
        double eff1 = congestionSolver.getTotalRemediedCongestion();

        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation, 8,
                5);
        partners = Lists.newArrayList();
        gd = new GammaDistribution(new MersenneTwister(1312421l),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        for (int i = 0; i < 200; i++) {
            partners.add(new DSMPartner((int) gd.sample()));
            congestionSolver.registerDSMPartner(partners.get(i));
        }
        // sim = Simulator.createSimulator(35040);
        // sim.register(congestionSolver);
        // sim.start();
        for (int i = 0; i < 3; i++) {
            congestionSolver.afterTick(1);
        }
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        for (int i = 0; i < 10; i++) {
            congestionSolver.tick(1);
            congestionSolver.afterTick(1);
        }
        double eff2 = congestionSolver.getTotalRemediedCongestion();
        // assertEquals(getTotalActs(partners), totalActsComp);
        System.out.println(eff1 + " " + eff2);
        assertTrue(eff1 < eff2);
    }

    @Test
    public void testScenarioManyAgents3() {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV("smalltest.csv", "test2");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        int N = 4;
        abstractTimeSeriesImplementation.changeValue(6, 500);
        abstractTimeSeriesImplementation.changeValue(7, 300);
        abstractTimeSeriesImplementation.changeValue(8, 900);
        abstractTimeSeriesImplementation.changeValue(9, 1200);
        abstractTimeSeriesImplementation.changeValue(9, 300);
        congestionSolver = new CompetitiveCongestionSolver(abstractTimeSeriesImplementation, 8,
                5);
        List<DSMPartner> partners = Lists.newArrayList();
        GammaDistribution gd = new GammaDistribution(
                new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                R3DP_GAMMA_SCALE);
        for (int i = 0; i < N; i++) {
            partners.add(new DSMPartner((int) gd.sample()));
            congestionSolver.registerDSMPartner(partners.get(i));
        }
        // sim = Simulator.createSimulator(499);
        // sim.register(congestionSolver);
        // sim.start();
        for (int i = 0; i < 3; i++) {
            congestionSolver.afterTick(1);
        }
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        for (int i = 0; i < 10; i++) {
            congestionSolver.tick(1);
            congestionSolver.afterTick(1);
        }
        int totalActsComp = getTotalActs(partners);
        double eff1 = congestionSolver.getTotalRemediedCongestion();

        double eff1R = getIAgentEff(partners, 25);
        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation, 8,
                5);
        partners = Lists.newArrayList();
        gd = new GammaDistribution(new MersenneTwister(1312421l),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        for (int i = 0; i < N; i++) {
            partners.add(new DSMPartner((int) gd.sample()));
            congestionSolver.registerDSMPartner(partners.get(i));
        }
        // sim = Simulator.createSimulator(35040);
        // sim.register(congestionSolver);
        // sim.start();
        for (int i = 0; i < 3; i++) {
            congestionSolver.afterTick(1);
        }
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        for (int i = 0; i < 10; i++) {
            congestionSolver.tick(1);
            congestionSolver.afterTick(1);
        }
        double eff2 = congestionSolver.getTotalRemediedCongestion();
        double eff2R = getIAgentEff(partners, 25);
        assertEquals(getTotalActs(partners), totalActsComp);
        System.out.println(eff1 + " " + eff2);
        System.out.println(eff1R + " " + eff2R);
        assertTrue(eff1R < eff2R);
        // assertTrue(eff1 < eff2); //allocation lower but eff higher.
    }

    @Test
    public void testVarParamLargeScen() {
        for (int i = 2; i <= 100; i++) {
            System.out.println("Scen with " + i + "agents.");
            testScenarioManyAgentsLargerScen(i);
        }
    }

    public void testScenarioManyAgentsLargerScen(int n) {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV(file, column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        final int nAgents = n;
        int length = 599;
        int allowed = 2;
        congestionSolver = new CompetitiveCongestionSolver(abstractTimeSeriesImplementation, 8,
                allowed);
        List<DSMPartner> partners = Lists.newArrayList();
        GammaDistribution gd = new GammaDistribution(
                new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                R3DP_GAMMA_SCALE);
        for (int i = 0; i < nAgents; i++) {
            int sample = (int) gd.sample();
            partners.add(new DSMPartner((int) gd.sample()));
            congestionSolver.registerDSMPartner(partners.get(i));
        }
        sim = Simulator.createSimulator(length);
        sim.register(congestionSolver);
        sim.start();
        int totalActsComp = getTotalActs(partners);
        double eff1 = congestionSolver.getTotalRemediedCongestion();
        double act1 = getActivationRate(partners);
        double eff1R = getIAgentEff(partners, length);
        double sumNeg1 = 0;
        for (double val : congestionSolver.getProfileAfterDSM().values()) {
            if (val < 0) {
                sumNeg1 += (val * -1);
            }
        }

        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation, 8,
                allowed);
        partners = Lists.newArrayList();
        gd = new GammaDistribution(new MersenneTwister(1312421l),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        for (int i = 0; i < nAgents; i++) {
            partners.add(new DSMPartner((int) gd.sample()));
            congestionSolver.registerDSMPartner(partners.get(i));
        }
        sim = Simulator.createSimulator(length);
        sim.register(congestionSolver);
        sim.start();
        double eff2 = congestionSolver.getTotalRemediedCongestion();
        double eff2R = getIAgentEff(partners, length);
        double act2 = getActivationRate(partners);
        double sumNeg2 = 0;
        for (double val : congestionSolver.getProfileAfterDSM().values()) {
            if (val < 0) {
                sumNeg2 += (val * -1);
            }
        }
        // assertTrue(getTotalActs(partners) <= totalActsComp);
        System.out.println(eff1 + " " + eff2);
        System.out.println(eff1R + " " + eff2R);
        System.out.println(sumNeg1 + " " + sumNeg2);
        // assertTrue(eff1R <= eff2R);
        // assertTrue(act1 <= act2);
        assertTrue(eff1 <= eff2); // allocation lower but eff higher.
        // assertTrue(sumNeg1 > sumNeg2);
    }

    private double getActivationRate(List<DSMPartner> partners) {
        int sum = 0;
        for (DSMPartner p : partners) {
            sum += p.getCurrentActivations();
        }
        return sum / (double) (partners.size()
                * partners.get(0).getMaxActivations());
    }

}

package be.kuleuven.cs.flexsim.domain.site;

import be.kuleuven.cs.flexsim.domain.process.FlexProcess;
import be.kuleuven.cs.flexsim.domain.util.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.Simulator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class EquidistantSiteSimulationTest {
    private Site site = mock(SiteSimulation.class);
    private final int BASE = 450, MIN = 300, MAX = 600, MAXTUPLES = 6;
    private final int SIMSTEPS = 10;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        site = SiteBuilder.newEquidistantSiteSimulation()
                .withBaseConsumption(BASE).withMinConsumption(MIN)
                .withMaxConsumption(MAX).withTuples(MAXTUPLES).create();
    }

    @Test
    public void testInit() {
        site = site = SiteBuilder.newEquidistantSiteSimulation()
                .withBaseConsumption(BASE).withMinConsumption(MIN)
                .withMaxConsumption(MAX).withTuples(MAXTUPLES).create();
        assertEquals(BASE, site.getAverageConsumption(), 0);
        assertEquals(BASE, site.getLastStepConsumption(), 0);
        assertEquals(0, site.getTotalConsumption(), 0);
        assertEquals(MAXTUPLES, site.getFlexTuples().size(), 0);
        assertEquals(0, site.getBufferOccupancyLevels().size());
        assertFalse(site.containsLine(mock(FlexProcess.class)));
        assertEquals(20 * ((double) (BASE - MIN) / (double) (MAX - MIN)),
                site.takeResources().size(), 0);
        thrown.expect(UnsupportedOperationException.class);
        site.deliverResources(mock(List.class));
    }

    @Test
    public void testGetTuples() {
        List<FlexTuple> tuples = site.getFlexTuples();
        assertEquals((MAX - MIN) / MAXTUPLES,
                Math.abs(tuples.get(0).getDeltaP() - tuples.get(1).getDeltaP()),
                0);
    }

    @Test
    public void testSim() {
        Simulator s = Simulator.createSimulator(SIMSTEPS);
        s.register(site);
        s.start();
        assertEquals(site.getLastStepConsumption() * SIMSTEPS,
                site.getTotalConsumption(), 0);
    }

    @Test
    public void testChangeConsumption() {
        Simulator s = Simulator.createSimulator(SIMSTEPS);
        s.register(site);
        s.start();
        s = Simulator.createSimulator(SIMSTEPS);
        s.register(site);
        s.start();

        double before = site.getTotalConsumption();

        site = site = SiteBuilder.newEquidistantSiteSimulation()
                .withBaseConsumption(BASE).withMinConsumption(MIN)
                .withMaxConsumption(MAX).withTuples(MAXTUPLES).create();
        s = Simulator.createSimulator(SIMSTEPS);
        s.register(site);
        s.start();
        final FlexTuple t = site.getFlexTuples().get(1);
        site.activateFlex(new ActivateFlexCommand() {
            @Override
            public long getReferenceID() {
                return t.getId();
            }
        });
        s = Simulator.createSimulator(SIMSTEPS);
        s.register(site);
        s.start();
        double after = site.getTotalConsumption();
        assertTrue(before > after);
    }

    @Test
    public void testTuples() {
        Simulator s = Simulator.createSimulator(SIMSTEPS);
        s.register(site);
        s.start();
        s = Simulator.createSimulator(SIMSTEPS);
        s.register(site);
        s.start();
        List<FlexTuple> tuples = site.getFlexTuples();
        for (int i = 3; i < tuples.size(); i++) {
            if (tuples.get(i).getDeltaP()
                    - tuples.get(i - 1).getDeltaP() != (MAX - MIN)
                            / MAXTUPLES) {
                fail();
            }
        }
        assertEquals(MAXTUPLES, tuples.size(), 0);
    }

    @Test
    public void testToString() {
        String res = site.toString();
        assertTrue(res.contains("SiteSimulation"));
        assertTrue(res.contains(String.valueOf(BASE)));
        assertTrue(res.contains(String.valueOf(MAXTUPLES)));
    }
}

package simulation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class SimulatorTest {
    private Simulator s;
    private ISimulationComponent comp;
    private final long defaultRunTime = 1;

    @Before
    public void setUp() throws Exception {
        s = Simulator.createSimulator(defaultRunTime);
        comp = mock(ISimulationComponent.class);
    }

    @Test
    public void testInitialState() {
        assertEquals(0, s.getSimulationTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDurationInit() {
        s = Simulator.createSimulator(0);  
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroDurationInit() {
        s = Simulator.createSimulator(0);  
    }

    @Test
    public void testStartedSim() {
        s.start();
        assertEquals(defaultRunTime, s.getSimulationTime());
        assertEquals(s.getDuration(), s.getSimulationTime());
    }

    @Test
    public void testSimDuration() {
        long duration = 20;
        s = Simulator.createSimulator(duration);
        assertEquals(duration, s.getDuration());
    }

    @Test
    public void testRegisterComp() {
        s.register(comp);
        assertEquals(1, s.getComponents().size());
    }

    @Test
    public void testRunDurationImmediateReturn() {
        long duration = 20;
        s = Simulator.createSimulator(duration);
        runSim(true);
        verify(comp, times(20)).tick();
    }

    private void runSim(boolean immediateReturn) {
        s.register(comp);
        s.start();
    }

}

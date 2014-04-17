package be.kuleuven.cs.flexsim.domain.finances;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.factory.ProductionLine;
import be.kuleuven.cs.flexsim.domain.factory.ProductionLineTest.ChangeEventComponent;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.flexsim.simulation.Simulator;

public class FinanceTrackerTest {
    private ProcessTrackableSimulationComponent mockPL = mock(ProcessTrackableSimulationComponent.class);
    private FinanceTracker t = new FinanceTracker(mockPL, RewardModel.CONSTANT,
            DebtModel.CONSTANT);
    private SimulationContext sim = mock(SimulationContext.class);

    @Before
    public void setUp() throws Exception {
        t = new FinanceTracker(mockPL, RewardModel.CONSTANT, DebtModel.CONSTANT);
        sim = Simulator.createSimulator(20);
    }

    @Test
    public void testConstructor() {
        assertFalse(t.getSimulationSubComponents().isEmpty());

    }

    @Test
    public void signalConsumptionTest() {
        ProductionLine mockPL = ProductionLine.createSimpleLayout();
        t = new FinanceTracker(mockPL, RewardModel.CONSTANT, DebtModel.CONSTANT);
        int n = 3;
        List<Resource> res = ResourceFactory.createBulkMPResource(n, 3, 1);
        mockPL.deliverResources(res);
        SimulationComponent m = mock(SimulationComponent.class);
        SimulationComponent tester = new ChangeEventComponent(m);
        long duration = 20;
        sim.register(t);
        sim.register(tester);
        ((Simulator) sim).start();
        verify(m, times((int) duration)).tick(0);
        assertEquals("simulation:stopped",
                ((ChangeEventComponent) tester).getLastType());
    }

    @Test
    public void getCurrentPaymentRateTest() {
        ProductionLine mockPL = ProductionLine.createSimpleLayout();
        t = new FinanceTracker(mockPL, RewardModel.CONSTANT, DebtModel.CONSTANT);
        int n = 3;
        List<Resource> res = ResourceFactory.createBulkMPResource(n, 3, 1);
        mockPL.deliverResources(res);
        sim.register(t);
        assertEquals(0, t.getTotalCost(), 0);
        ((Simulator) sim).start();
        assertNotEquals(0, t.getTotalCost());
    }

    @Test
    public void getCurrentRewardRateTest() {
        ProductionLine mockPL = ProductionLine.createSimpleLayout();
        t = new FinanceTracker(mockPL, RewardModel.CONSTANT, DebtModel.CONSTANT);
        int n = 3;
        List<Resource> res = ResourceFactory.createBulkMPResource(n, 3, 1);
        mockPL.deliverResources(res);
        sim.register(t);
        assertEquals(0, t.getTotalReward(), 0);
        ((Simulator) sim).start();
        assertNotEquals(0, t.getTotalReward());
        assertEquals(
                0,
                mockPL.getBufferOccupancyLevels().get(
                        mockPL.getBufferOccupancyLevels().size() - 1), 0);
    }

    @Test
    public void getProfitTest() {
        ProductionLine mockPL = ProductionLine.createSimpleLayout();
        t = new FinanceTracker(mockPL, RewardModel.CONSTANT, DebtModel.CONSTANT);
        int n = 3;
        List<Resource> res = ResourceFactory.createBulkMPResource(n, 3, 1);
        mockPL.deliverResources(res);
        sim.register(t);
        assertEquals(0, t.getTotalReward(), 0);
        ((Simulator) sim).start();
        int reward = t.getTotalReward();
        int cost = t.getTotalCost();
        assertEquals(reward - cost, t.getTotalProfit());
    }
}

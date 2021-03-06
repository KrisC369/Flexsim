package be.kuleuven.cs.gridflex.domain.aggregation;

import be.kuleuven.cs.gridflex.domain.aggregation.independent.IndependentAggregator;
import be.kuleuven.cs.gridflex.domain.energy.tso.BalancingSignal;
import be.kuleuven.cs.gridflex.domain.site.SiteFlexAPI;
import be.kuleuven.cs.gridflex.domain.util.FlexTuple;
import be.kuleuven.cs.gridflex.simulation.Simulator;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AggregatorTest {
    private BalancingSignal tso = mock(BalancingSignal.class);
    private int freq = 10;
    private IndependentAggregator agg = new IndependentAggregator(tso, freq);
    private SiteFlexAPI clientDown = mock(SiteFlexAPI.class);
    private SiteFlexAPI clientUp = mock(SiteFlexAPI.class);
    private Simulator sim = Simulator.createSimulator(2 * freq - 2);

    @Before
    public void setUp() throws Exception {
        doReturn(0).when(tso).getCurrentImbalance();
        this.agg = new IndependentAggregator(tso, freq);
        this.clientDown = mock(SiteFlexAPI.class);
        doReturn(Lists.newArrayList(FlexTuple.create(1, 5, FlexTuple.Direction.DOWN, 10, 0, 0),
                FlexTuple.create(3, 10, FlexTuple.Direction.UP, 10, 0, 0))).when(clientDown)
                .getFlexTuples();

        this.clientUp = mock(SiteFlexAPI.class);
        doReturn(Lists.newArrayList(FlexTuple.create(2, 5, FlexTuple.Direction.UP, 10, 0, 0),
                FlexTuple.create(4, 10, FlexTuple.Direction.DOWN, 10, 0, 0))).when(clientUp)
                .getFlexTuples();

        doRegister();
    }

    @Test
    public void testRegisterClients() {
        this.agg = new IndependentAggregator(tso, freq);
        agg.registerClient(clientUp);
        agg.registerClient(clientDown);
        assertEquals(2, agg.getClients().size());
        assertTrue(agg.getClients().contains(clientUp));
        assertTrue(agg.getClients().contains(clientDown));
        doRegister();
        assertEquals(2, agg.getClients().size());
    }

    private void doRegister() {
        agg.registerClient(clientUp);
        agg.registerClient(clientDown);
        sim.register(agg);
    }

    @Test
    public void testFilterPositive() {
        testFilter(FlexTuple.Direction.UP);
    }

    @Test
    public void testFilterNegative() {
        testFilter(FlexTuple.Direction.DOWN);
    }

    private void testFilter(FlexTuple.Direction b) {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> sorted = LinkedListMultimap
                .create();
        sorted.putAll(clientDown, clientDown.getFlexTuples());
        sorted.putAll(clientUp, clientDown.getFlexTuples());
        AggregationUtils.filter(sorted, b);
        assertEquals(1, sorted.get(clientDown).size(), 0);
        assertEquals(1, sorted.get(clientUp).size(), 0);
        assertEquals(b, sorted.get(clientDown).get(0).getDirection());
        assertEquals(b, sorted.get(clientUp).get(0).getDirection());
    }
}

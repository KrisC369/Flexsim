package be.kuleuven.cs.flexsim.domain.aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.site.ActivateFlexCommand;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.tso.SteeringSignal;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.Simulator;

import com.google.common.collect.Lists;

public class AggregatorImplTest {
    SteeringSignal tso = mock(SteeringSignal.class);
    private int freq = 10;
    private AggregatorImpl agg = new AggregatorImpl(tso, freq);
    private SiteFlexAPI clientDown = mock(SiteFlexAPI.class);
    private SiteFlexAPI clientUp = mock(SiteFlexAPI.class);
    private Simulator sim = Simulator.createSimulator(2 * freq - 2);

    @Before
    public void setUp() throws Exception {
        doReturn(0).when(tso).getCurrentValue(anyInt());
        this.agg = new AggregatorImpl(tso, freq);
        this.clientDown = mock(SiteFlexAPI.class);
        doReturn(
                Lists.newArrayList(FlexTuple.create(1, 5, false, 10, 0, 0),
                        FlexTuple.create(3, 10, true, 10, 0, 0))).when(
                clientDown).getFlexTuples();

        this.clientUp = mock(SiteFlexAPI.class);
        doReturn(
                Lists.newArrayList(FlexTuple.create(2, 5, true, 10, 0, 0),
                        FlexTuple.create(4, 10, false, 10, 0, 0))).when(
                clientUp).getFlexTuples();

        doRegister();
    }

    @Test
    public void testCreation() {
        this.agg = new AggregatorImpl(tso, freq);
        assertEquals(0, agg.getClients().size());
        assertEquals(0, agg.getTso().getCurrentValue(0));
    }

    @Test
    public void testRegisterClients() {
        this.agg = new AggregatorImpl(tso, freq);
        agg.registerClient(clientUp);
        agg.registerClient(clientDown);
        assertEquals(2, agg.getClients().size());
        assertTrue(agg.getClients().contains(clientUp));
        assertTrue(agg.getClients().contains(clientDown));
        doRegister();
        assertEquals(2, agg.getClients().size());
    }

    @Test
    public void testSimpleAggregationNoFlex() {
        doRegister();
        sim.start();
        verify(clientUp, times(0)).activateFlex(any(ActivateFlexCommand.class));
        verify(clientDown, times(0)).activateFlex(
                any(ActivateFlexCommand.class));
    }

    @Test
    public void testSimpleAggregationUpFlex() {
        tso = mock(SteeringSignal.class);
        doReturn(5).when(tso).getCurrentValue(anyInt());
        agg = new AggregatorImpl(tso, freq);
        doRegister();
        sim.start();
        verify(clientUp, times(1)).activateFlex(any(ActivateFlexCommand.class));
        verify(clientDown, times(0)).activateFlex(
                any(ActivateFlexCommand.class));
    }

    private void doRegister() {
        agg.registerClient(clientUp);
        agg.registerClient(clientDown);
        sim.register(agg);
    }

    @Test
    public void testSimpleAggregationDownFlex() {
        tso = mock(SteeringSignal.class);
        doReturn(-5).when(tso).getCurrentValue(anyInt());
        agg = new AggregatorImpl(tso, freq);
        doRegister();
        sim.start();
        verify(clientUp, times(0)).activateFlex(any(ActivateFlexCommand.class));
        verify(clientDown, times(1)).activateFlex(
                any(ActivateFlexCommand.class));
    }

    @Test
    public void testSimpleAggregationDoubleDownFlex() {
        tso = mock(SteeringSignal.class);
        doReturn(15).when(tso).getCurrentValue(anyInt());
        agg = new AggregatorImpl(tso, freq);
        doRegister();
        sim.start();
        verify(clientUp, times(1)).activateFlex(any(ActivateFlexCommand.class));
        verify(clientDown, times(1)).activateFlex(
                any(ActivateFlexCommand.class));
    }

    @Test
    public void testSimpleAggregationDoubleUpFlex() {
        tso = mock(SteeringSignal.class);
        doReturn(-15).when(tso).getCurrentValue(anyInt());
        agg = new AggregatorImpl(tso, freq);
        doRegister();
        sim.start();
        verify(clientUp, times(1)).activateFlex(any(ActivateFlexCommand.class));
        verify(clientDown, times(1)).activateFlex(
                any(ActivateFlexCommand.class));
    }

    @Test
    public void testUseOnlyOneProfile() {
        tso = mock(SteeringSignal.class);
        this.clientDown = mock(SiteFlexAPI.class);
        doReturn(
                Lists.newArrayList(FlexTuple.create(1, 5, false, 10, 0, 0),
                        FlexTuple.create(7, 5, false, 10, 0, 0),
                        FlexTuple.create(3, 10, true, 10, 0, 0))).when(
                clientDown).getFlexTuples();
        doReturn(5000).when(tso).getCurrentValue(anyInt());
        agg = new AggregatorImpl(tso, freq);
        doRegister();
        sim.start();
        verify(clientDown, times(1)).activateFlex(
                any(ActivateFlexCommand.class));
    }
}

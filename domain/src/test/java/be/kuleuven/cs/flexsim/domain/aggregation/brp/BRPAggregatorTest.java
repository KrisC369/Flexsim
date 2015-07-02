package be.kuleuven.cs.flexsim.domain.aggregation.brp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingSignal;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteBuilder;
import be.kuleuven.cs.flexsim.simulation.Simulator;

public class BRPAggregatorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private static final long _400 = 400;
    private BRPAggregator agg = mock(BRPAggregator.class);
    private Site site1 = mock(Site.class);
    private Site site2 = mock(Site.class);
    private Simulator sim = Simulator.createSimulator(1);
    private BalancingSignal tso = mock(BalancingSignal.class);
    private PriceSignal price = mock(PriceSignal.class);

    @Before
    public void setUp() throws Exception {
        agg = new BRPAggregator(tso, price, 0.5, 0.5);
        site1 = SiteBuilder.newSiteSimulation().withBaseConsumption(200).withMaxConsumption(400).withTuples(2).create();
        site2 = SiteBuilder.newSiteSimulation().withBaseConsumption(200).withMaxConsumption(400).withTuples(2).create();
        sim = Simulator.createSimulator(1);
    }

    @Test
    public void testRegisterPaymMediator() {
        agg.registerClient(site1);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(site1);
        assertNotNull(t);
        assertTrue(agg.getClients().contains(site1));
        assertTrue(t.getTarget().equals(site1));
    }

    @Test
    public void testSetBudget() {
        agg.registerClient(site1);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(site1);
        t.setBudget(_400);
        assertEquals(_400, t.getCurrentBudget());
    }

    @Test
    public void testDoReservationPayment() {
        agg.registerClient(site1);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(site1);
        sim.register(t);
        t.setBudget(_400);
        double proportion = 0.3;
        t.registerReservation(proportion);
        assertEquals(_400 * proportion * 0.5, t.getTotalProfit(), 0.01);
        sim.start();
    }

    @Test
    public void testDoActivationPayment() {
        agg.registerClient(site1);

        sim.register(agg);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(site1);
        sim.register(t);
        t.setBudget(_400);
        double proportion = 0.3;
        t.registerActivation(proportion);
        assertEquals(_400 * proportion * 0.5, t.getTotalProfit(), 0.01);
        sim.start();
    }

    @Test
    public void testFullReservationPayment1() {
        sim.register(agg);
        agg.registerClient(site1);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(site1);
        when(tso.getCurrentImbalance()).thenReturn((int) _400);
        when(price.getCurrentPrice()).thenReturn(1);

        sim.register(t);
        // t.setBudget(_400);
        sim.start();

        assertEquals(_400 * 0.5 * (200 / (double) _400), t.getTotalProfit(), 0.01);
    }

    @Test
    public void testFullReservationPayment2() {
        sim.register(agg);
        agg.registerClient(site1);
        agg.registerClient(site2);
        RenumerationMediator t1 = agg.getActualPaymentMediatorFor(site1);
        RenumerationMediator t2 = agg.getActualPaymentMediatorFor(site2);
        when(tso.getCurrentImbalance()).thenReturn((int) _400);
        when(price.getCurrentPrice()).thenReturn(1);

        sim.register(t1);
        sim.register(t2);
        t1.setBudget(_400);
        t2.setBudget(_400);

        sim.start();

        double proportion = 0.5;
        assertEquals(_400 * proportion, t1.getTotalProfit(), 0.01);
        assertEquals(_400 * proportion, t2.getTotalProfit(), 0.01);
    }

    @Test
    public void testFullActivationPayment1() {
        agg.registerClient(site1);
        sim.register(agg);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(site1);
        sim.register(t);
        t.setBudget(_400);
        when(tso.getCurrentImbalance()).thenReturn((int) _400);
        when(price.getCurrentPrice()).thenReturn(1);
        sim.start();

        assertEquals(2 * _400 * 0.5 * (200 / (double) _400) + (_400 * 0.5 * (200 / (double) _400)), t.getTotalProfit(),
                0.01);
    }

    @Test
    public void testFullActivationPayment2() {
        sim.register(agg);
        agg.registerClient(site1);
        agg.registerClient(site2);
        RenumerationMediator t1 = agg.getActualPaymentMediatorFor(site1);
        RenumerationMediator t2 = agg.getActualPaymentMediatorFor(site2);

        sim.register(t1);
        sim.register(t2);
        t1.setBudget(_400);
        t2.setBudget(_400);
        when(tso.getCurrentImbalance()).thenReturn((int) _400);
        when(price.getCurrentPrice()).thenReturn(1);
        sim.start();

        double proportion = 0.5;
        assertEquals(_400 * proportion, t1.getTotalProfit(), 0.01);
        assertEquals(_400 * proportion, t2.getTotalProfit(), 0.01);
    }

    @Test
    public void testPaymentMediator1() {
        sim.register(agg);
        agg.registerClient(site1);
        agg.registerClient(site2);
        RenumerationMediator t1 = agg.getActualPaymentMediatorFor(site1);
        thrown.expect(IllegalArgumentException.class);
        t1.registerActivation(1.1);
    }

    @Test
    public void testPaymentMediator2() {
        sim.register(agg);
        agg.registerClient(site1);
        agg.registerClient(site2);
        RenumerationMediator t1 = agg.getActualPaymentMediatorFor(site1);
        thrown.expect(IllegalArgumentException.class);
        t1.registerActivation(-0.5);
    }

    @Test
    public void testPaymentMediator3() {
        sim.register(agg);
        agg.registerClient(site1);
        agg.registerClient(site2);
        RenumerationMediator t1 = agg.getActualPaymentMediatorFor(site1);
        thrown.expect(IllegalArgumentException.class);
        t1.registerReservation(1.1);
    }

    @Test
    public void testPaymentMediator4() {
        sim.register(agg);
        agg.registerClient(site1);
        agg.registerClient(site2);
        RenumerationMediator t1 = agg.getActualPaymentMediatorFor(site1);
        thrown.expect(IllegalArgumentException.class);
        t1.registerReservation(-0.5);
    }

    @Test
    public void testNomination() {
        sim.register(agg);
        agg.registerClient(site1);
        agg.registerClient(site2);
        AncilServiceNominationManager asnm = mock(AncilServiceNominationManager.class);
        agg.registerNominationManager(asnm);
        sim.start();
        verify(asnm, times(1)).registerNomination(any(Nomination.class));
    }
}

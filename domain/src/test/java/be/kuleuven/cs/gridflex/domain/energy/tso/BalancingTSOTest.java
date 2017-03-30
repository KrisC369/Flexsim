package be.kuleuven.cs.gridflex.domain.energy.tso;

import be.kuleuven.cs.gridflex.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.gridflex.domain.energy.tso.contractual.BalancingTSO;
import be.kuleuven.cs.gridflex.domain.energy.tso.contractual.ContractualMechanismParticipant;
import be.kuleuven.cs.gridflex.domain.util.data.IntPowerCapabilityBand;
import be.kuleuven.cs.gridflex.simulation.Simulator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class BalancingTSOTest {

    private BalancingTSO tso = mock(BalancingTSO.class);
    private Simulator sim = Simulator.createSimulator(1);
    private double imbalance = 1000d;
    private int steps = 1;
    public static @DataPoints double[] candidates = { 1000d, -1000d, 0 };

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        EnergyProductionTrackable prod = mock(EnergyProductionTrackable.class);
        when(prod.getLastStepProduction()).thenReturn(imbalance);
        tso = new BalancingTSO(prod);
        sim = Simulator.createSimulator(steps);
        sim.register(tso);
    }

    @Test
    @Theory
    public void testImbalanceValueInit(double candidate) {
        imbalance = candidate;
        try {
            setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sim.start();
        assertEquals(imbalance, tso.getCurrentImbalance(), 0);
    }

    @Test
    public void testRegisterClients() {
        ContractualMechanismParticipant agg = mock(
                ContractualMechanismParticipant.class);
        tso.registerParticipant(agg);
        when(agg.getPowerCapacity())
                .thenReturn(IntPowerCapabilityBand.createZero());
        assertTrue(tso.getParticipants().contains(agg));
        assertTrue(tso.getContractualLimit(agg).isZero());
        ContractualMechanismParticipant agg2 = new ContractualMechanismParticipant() {
            @Override
            public void signalTarget(int timestep, int target) {
            }

            @Override
            public IntPowerCapabilityBand getPowerCapacity() {
                return IntPowerCapabilityBand.create(20, 20);
            }
        };
        tso.registerParticipant(agg2);
        tso.afterTick(0);
        assertTrue(tso.getParticipants().contains(agg2));
        assertFalse(tso.getContractualLimit(agg2).isZero());
        ContractualMechanismParticipant agg3 = new ContractualMechanismParticipant() {
            @Override
            public void signalTarget(int timestep, int target) {
            }

            @Override
            public IntPowerCapabilityBand getPowerCapacity() {
                return IntPowerCapabilityBand.create(0, 20);
            }
        };
        tso.registerParticipant(agg3);
        tso.afterTick(0);
        assertTrue(tso.getParticipants().contains(agg3));
        assertFalse(tso.getContractualLimit(agg3).isZero());
        ContractualMechanismParticipant agg4 = new ContractualMechanismParticipant() {
            @Override
            public void signalTarget(int timestep, int target) {
            }

            @Override
            public IntPowerCapabilityBand getPowerCapacity() {
                return IntPowerCapabilityBand.create(20, 0);
            }
        };
        tso.registerParticipant(agg4);
        tso.afterTick(0);
        assertTrue(tso.getParticipants().contains(agg4));
        assertFalse(tso.getContractualLimit(agg4).isZero());
    }

    @Test
    public void testWrongRegisterClients() {
        ContractualMechanismParticipant agg = mock(
                ContractualMechanismParticipant.class);
        exception.expect(IllegalStateException.class);
        assertFalse(tso.getParticipants().contains(agg));
        assertTrue(tso.getContractualLimit(agg).isZero());
    }

    @Test
    public void testSignalPowerFlex() {
        ContractualMechanismParticipant agg = mock(
                ContractualMechanismParticipant.class);
        tso.registerParticipant(agg);
        IntPowerCapabilityBand cap = IntPowerCapabilityBand.create(50, 100);
        tso.signalNewLimits(agg, cap);
        assertEquals(cap, tso.getContractualLimit(agg));
    }

    @Test
    public void testActivation1() {
        int capS1 = 40;
        int capS2 = 400;
        ContractualMechanismParticipant agg1 = mock(
                ContractualMechanismParticipant.class);
        tso.registerParticipant(agg1);
        ContractualMechanismParticipant agg2 = mock(
                ContractualMechanismParticipant.class);
        tso.registerParticipant(agg2);
        IntPowerCapabilityBand cap1 = IntPowerCapabilityBand.create(capS1, capS1 * 2);
        IntPowerCapabilityBand cap2 = IntPowerCapabilityBand.create(capS2, capS2 * 2);
        when(agg1.getPowerCapacity()).thenReturn(cap1);
        when(agg2.getPowerCapacity()).thenReturn(cap2);
        tso.signalNewLimits(agg1, cap1);
        tso.signalNewLimits(agg2, cap2);
        sim.start();
        verify(agg1, times(1)).signalTarget(anyInt(), eq(capS1 * 2));
        verify(agg2, times(1)).signalTarget(anyInt(), eq(capS2 * 2));
    }

    @Test
    public void testActivation2() {
        int capS1 = 500;
        int capS2 = 5000;
        double factor = 0.0909090909;
        ContractualMechanismParticipant agg1 = mock(
                ContractualMechanismParticipant.class);
        tso.registerParticipant(agg1);
        ContractualMechanismParticipant agg2 = mock(
                ContractualMechanismParticipant.class);
        tso.registerParticipant(agg2);
        IntPowerCapabilityBand cap1 = IntPowerCapabilityBand.create(capS1, capS1 * 2);
        IntPowerCapabilityBand cap2 = IntPowerCapabilityBand.create(capS2, capS2 * 2);
        when(agg1.getPowerCapacity()).thenReturn(cap1);
        when(agg2.getPowerCapacity()).thenReturn(cap2);
        tso.signalNewLimits(agg1, cap1);
        tso.signalNewLimits(agg2, cap2);
        sim.start();
        verify(agg1, times(1)).signalTarget(anyInt(),
                eq((int) Math.round(capS1 * 2 * factor)));
        verify(agg2, times(1)).signalTarget(anyInt(),
                eq((int) Math.round(capS2 * 2 * factor)));
    }
}

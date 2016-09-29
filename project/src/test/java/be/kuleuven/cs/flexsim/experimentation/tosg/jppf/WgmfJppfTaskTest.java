package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.experimentation.tosg.ExperimentParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.WindBasedInputData;
import be.kuleuven.cs.gametheory.GameInstanceResult;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.LOCAL;
import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.REMOTE;
import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.WgmfGameRunner.loadResources;
import static be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver.Solver.DUMMY;
import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfJppfTaskTest {
    private static final int SEED = 3722;
    private static final String DISTRIBUTIONFILE = "windspeedDistributions.csv";
    private static final String DATAFILE = "2kwartOpEnNeer.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices.csv";
    private WgmfJppfTask task;
    private final String PARAMS = "test";
    private ExperimentParams expP;

    @Before
    public void setUp() {
        WindBasedInputData dataIn = null;
        try {
            dataIn = WindBasedInputData.loadFromResource(DATAFILE);
            TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);
            ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(IMBAL);
            ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                    .loadFromCSV(DISTRIBUTIONFILE);

            WgmfGameParams params = WgmfGameParams
                    .create(dataIn, new WgmfSolverFactory(
                            DUMMY), specs, distribution, imbalIn);
            GameInstanceConfiguration config = GameInstanceConfiguration.builder().setAgentSize(3)
                    .setActionSize(2)
                    .fixAgentToAction(0, 0).fixAgentToAction(1, 0).fixAgentToAction(2, 1)
                    .setSeed(231L).build();

            this.task = new WgmfJppfTask(config, params);
            this.expP = ExperimentParams.create(2, 1, DUMMY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRun() {
        task.run();
        GameInstanceResult result = task.getResult();
        assertEquals(0, result.getPayoffs().get(0), 0);
    }

    @Test
    public void testRunLocal() {
        WgmfGameRunner gameJPPFRunner = new WgmfGameRunner(expP, LOCAL);
        WgmfGameParams params = loadResources(expP);
        gameJPPFRunner.execute(params);
    }

    @Test
    public void testRunRemote() {
        WgmfGameRunner gameJPPFRunner = new WgmfGameRunner(expP, REMOTE);
        WgmfGameParams params = loadResources(expP);
        gameJPPFRunner.execute(params);
    }
}
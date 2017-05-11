package be.kuleuven.cs.gridflex.domain.util.data;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WindSpeedForecastMultiHorizonErrorDistributionTest {

    public static final String FILENAME = "windspeedDistributions.csv";

    @Test
    public void testLoadFromFile() throws IOException {
        WindSpeedForecastMultiHorizonErrorDistribution testDistro = WindSpeedForecastMultiHorizonErrorDistribution
                .loadFromCSV(FILENAME);
        assertEquals(36, testDistro.getMaxForecastHorizon(), 0);
    }
}
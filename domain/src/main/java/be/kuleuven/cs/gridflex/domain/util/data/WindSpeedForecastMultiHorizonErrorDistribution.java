package be.kuleuven.cs.gridflex.domain.util.data;

import com.google.auto.value.AutoValue;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Value class for Error distributions for different forecast horizons.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class WindSpeedForecastMultiHorizonErrorDistribution
        extends AbstractErrorDistribution implements Serializable {
    private static final double KMH_TO_MS = 3.6d;

    WindSpeedForecastMultiHorizonErrorDistribution() {
    }

    /**
     * Load forecast horizon error distribution from csv resource.
     *
     * @param filename The name of the resource to load from.
     * @return A fully instantiated value object.
     * @throws IOException e.g. If the file cannot be found.
     */
    public static WindSpeedForecastMultiHorizonErrorDistribution loadFromCSV(String filename)
            throws IOException {
        ErrorDistributions errorDistributions = new ErrorDistributions(filename).invoke();
        List<Double> means = errorDistributions.getMeans();
        List<Double> sds = errorDistributions.getSds();
        //Apply correction: converting from km/h to m/s
        means = means.stream().map(v -> v / KMH_TO_MS).collect(Collectors.toList());
        sds = sds.stream().map(v -> v / KMH_TO_MS).collect(Collectors.toList());
        LoggerFactory.getLogger(PowerForecastMultiHorizonErrorDistribution.class)
                .debug("Loading power distribution from csv with entries: mean {}, sd {}",
                        means.get(0).toString(), sds.get(0).toString());
        return create(means, sds);
    }

    /**
     * Default factory method.
     *
     * @param means The means.
     * @param sds   The standard deviations.
     * @return A fully instantiated value object.
     */
    static WindSpeedForecastMultiHorizonErrorDistribution create(List<Double> means,
            List<Double> sds) {
        checkArgument(means.size() == sds.size(), "Should have as many mean values as SD values.");
        return new AutoValue_WindSpeedForecastMultiHorizonErrorDistribution(means, sds);
    }
}

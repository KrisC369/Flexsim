package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.util.data.profiles.NetRegulatedVolumeProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.PositiveImbalancePriceProfile;
import com.google.auto.value.AutoValue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a value class for representing input data profiles from one same dataset.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class ImbalancePriceInputData implements Serializable{

    private static final String NRV_COLUMN_KEY = "NRV";
    private static final String PPOS_COLUMN_KEY = "PPOS";

    ImbalancePriceInputData() {
    }

    public abstract NetRegulatedVolumeProfile getNetRegulatedVolumeProfile();

    public abstract PositiveImbalancePriceProfile getPositiveImbalancePriceProfile();

    /**
     * Load both defined profiles from resource file.
     *
     * @param filename the name of the resource (file) to load from.
     * @return
     * @throws IOException           If reading from the file is not possible.
     * @throws FileNotFoundException If the file with that name cannot be found.
     */
    public static ImbalancePriceInputData loadFromResource(String filename) throws IOException {
        NetRegulatedVolumeProfile nrv = NetRegulatedVolumeProfile
                .createFromCSV(filename, NRV_COLUMN_KEY);
        PositiveImbalancePriceProfile pos = PositiveImbalancePriceProfile
                .createFromCSV(filename, PPOS_COLUMN_KEY);
        return new AutoValue_ImbalancePriceInputData(nrv, pos);
    }

}

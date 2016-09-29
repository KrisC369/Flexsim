package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.util.Map;

/**
 * Representation of Game instance results with mapping from agentID to payoff.
 * Serializable for wire transport.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class GameInstanceResult implements Serializable {

    /**
     * @return The game instance config these results pertain to.
     */
    public abstract GameInstanceConfiguration getGameInstanceConfig();

    /**
     * @return The payoff mapping.
     */
    public abstract Map<Integer, Long> getPayoffs();

    /**
     * Static factory method.
     *
     * @param config The configuration.
     * @param po     The payoff mapping.
     * @return a game instance result mapping.
     */
    public static GameInstanceResult create(GameInstanceConfiguration config,
            Map<Integer, Long> po) {
        return new AutoValue_GameInstanceResult(config, po);
    }
}

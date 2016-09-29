package be.kuleuven.cs.gametheory.configurable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;

/**
 * Represents the configuration of a single playable game instance.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class GameInstanceConfiguration implements Serializable {
    private static final long DEF_SEED = 12345L;

    GameInstanceConfiguration() {
    }

    /**
     * @return The size of the agent pool.
     */
    public abstract int getAgentSize();

    /**
     * @return The size of the action pool.
     */
    public abstract int getActionSize();

    /**
     * @return The particular seed used in this instance.
     */
    public abstract long getSeed();

    /**
     * @return The mapping of agents to actions.
     */
    public abstract ImmutableMap<Integer, Integer> getAgentActionMap();

    /**
     * @return A builder instance.
     */
    public static Builder builder() {
        return new AutoValue_GameInstanceConfiguration.Builder().setSeed(DEF_SEED);
    }

    abstract Builder toBuilder();

    /**
     * Create a new value object which only differs from the current one by adding one mapping.
     *
     * @param tokenAgent  The agentID.
     * @param tokenAction The actionID.
     * @return The value object representing the configuration.
     */
    public GameInstanceConfiguration withAgentToAction(int tokenAgent, int tokenAction) {
        return toBuilder().fixAgentToAction(tokenAgent, tokenAction).build();
    }

    /**
     * Builder class.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setAgentSize(int value);

        public abstract Builder setActionSize(int value);

        public abstract Builder setSeed(long value);

        abstract ImmutableMap.Builder<Integer, Integer> agentActionMapBuilder();

        public Builder fixAgentToAction(int tokenAgent, int tokenAction) {
            agentActionMapBuilder().put(tokenAgent, tokenAction);
            return this;
        }

        public abstract GameInstanceConfiguration build();
    }
}

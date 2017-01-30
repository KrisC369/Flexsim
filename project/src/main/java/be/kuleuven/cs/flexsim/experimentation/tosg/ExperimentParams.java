package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.solvers.Solvers;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

/**
 * Experiment parameters that define the configuration of concrete experiments.
 * It is up to the implementing classes to define what the P* paramter values are used for.
 */
@AutoValue
public abstract class ExperimentParams implements Serializable {

    private static final int DEF_P1STEP = 1;
    private static final int DEF_P1END = 1;
    private static final int DEF_P1START = 0;
    private static final int DEF_WINDPROF = -1;
    private static final int DEF_DATAPROF = -1;

    ExperimentParams() {
    }

    /**
     * @return The number of agents to use.
     */
    public abstract int getNAgents();

    /**
     * @return The number of repititions to perform.
     */
    public abstract int getNRepititions();

    /**
     * @return The solvers to use.
     */
    public abstract Solvers.TYPE getSolver();

    /**
     * @return true if remote execution should be enabled.
     */
    public abstract boolean isRemoteExecutable();

    /**
     * @return The first parameter starting value.
     */
    public abstract double getP1Start();

    /**
     * @return The first parameter value increment step.
     */
    public abstract double getP1Step();

    /**
     * @return The first parameter stop condition value.
     */
    public abstract double getP1End();

    /**
     * @return The index of which hard coded profile to use for wind errors.
     */
    public abstract int getWindErrorProfileIndex();

    /**
     * @return The index of which hard coded profile to use for current congestion data.
     */
    public abstract int getCurrentDataProfileIndex();

    /**
     * @return A builder instance.
     */
    public static ExperimentParams.Builder builder() {
        return new AutoValue_ExperimentParams.Builder().setP1Start(DEF_P1START)
                .setP1Step(DEF_P1STEP).setP1End(DEF_P1END).setRemoteExecutable(false)
                .setWindErrorProfileIndex(DEF_WINDPROF).setCurrentDataProfileIndex(DEF_DATAPROF);
    }

    /**
     * Builder class.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        /**
         * @param value The value for this parameter.
         * @return This builder.
         */
        public abstract Builder setNAgents(int value);

        /**
         * @param value The value for this parameter.
         * @return This builder.
         */
        public abstract Builder setNRepititions(int value);

        /**
         * @param solver The solvers to use.
         * @return This builder.
         */
        public abstract Builder setSolver(Solvers.TYPE solver);

        /**
         * @param remote The value for this parameter.
         * @return This builder.
         */
        public abstract Builder setRemoteExecutable(boolean remote);

        /**
         * @param value The value for this parameter.
         * @return This builder.
         */
        public abstract Builder setP1Start(double value);

        /**
         * @param value The value for this parameter.
         * @return This builder.
         */
        public abstract Builder setP1Step(double value);

        /**
         * @param value The value for this parameter.
         * @return This builder.
         */
        public abstract Builder setP1End(double value);

        /**
         * @param idx The index to set in this param object.
         * @return This builder.
         */
        public abstract Builder setWindErrorProfileIndex(int idx);

        /**
         * @param idx The index to set in this param object.
         * @return This builder.
         */
        public abstract Builder setCurrentDataProfileIndex(int idx);

        /**
         * @return Builds an experimentparams instance.
         */
        public abstract ExperimentParams build();
    }
}

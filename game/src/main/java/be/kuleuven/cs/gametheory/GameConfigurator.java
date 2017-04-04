package be.kuleuven.cs.gametheory;

/**
 * Configuration provider. This is a factory for generating participating
 * elements according to the implemented generator interfaces.
 *
 * @param <S> The type of agent for the agent generator.
 * @param <A> The type of action for the gameinstance generator.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface GameConfigurator<S, A>
        extends AgentGenerator<S>, GameInstanceGenerator<S, A> {

}
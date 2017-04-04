package be.kuleuven.cs.gridflex.domain.util.listener;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <A>
 *            The type of the event info for this listener.
 */@FunctionalInterface
public interface Listener<A> {
    /**
     * The component this listener is subscribed to, triggered an event.
     * 
     * @param arg
     *            the argument describing the change.
     */
    void eventOccurred(A arg);
}

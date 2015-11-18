package be.kuleuven.cs.flexsim.protocol;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The type of the responder object.
 */
public interface Responder<T> {
    /**
     * @param responder
     * @param arg
     */
    void callForProposal(AnswerAnticipator<T> responder, T arg);

}

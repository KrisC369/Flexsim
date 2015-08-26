package be.kuleuven.cs.flexsim.protocol.contractnet;

import java.util.List;

import be.kuleuven.cs.flexsim.protocol.Proposal;

public class TestConcreteCNPInitiator extends CNPInitiator {
    public TestConcreteCNPInitiator() {
        super();
    }

    @Override
    public Proposal findBestProposal(List<Proposal> props, Proposal description) {
        return props.get(0);
    }

    @Override
    protected void signalNoSolutionFound() {
    }
}

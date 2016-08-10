package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.FlexProvider;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.sf.jmpi.main.MpConstraint;
import net.sf.jmpi.main.MpDirection;
import net.sf.jmpi.main.MpOperator;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.expression.MpExpr;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.sf.jmpi.main.expression.MpExpr.prod;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DSOOptimalSolver extends AbstractOptimalSolver {
    private static final String CONG = "Cong:";
    private static final String SOLVED = "Solved";
    private static final String ALLOC = "alloc:";
    private static final String FLEX = "Flex";
    private final CongestionProfile profile;
    private final Solver solver;
    private final List<String> congID;
    private final List<String> solvedID;
    private final Map<FlexProvider, String> flexID;
    private final ListMultimap<FlexProvider, String> allocDvarID;
    private AllocResults results;

    public DSOOptimalSolver(final CongestionProfile c, final Solver s) {
        profile = c;
        solver = s;
        congID = Lists.newArrayList();
        solvedID = Lists.newArrayList();
        flexID = Maps.newLinkedHashMap();
        allocDvarID = ArrayListMultimap.create();
        for (int i = 0; i < profile.length(); i++) {
            congID.add(CONG + i);
        }
        for (int i = 0; i < profile.length(); i++) {
            solvedID.add(SOLVED + ":" + i);
        }
    }

    @Override
    protected void processResults(final Optional<MpResult> result) {
        if (result.isPresent()) {
            final MpResult concreteResult = result.get();
            System.out.println(result);
            final List<Boolean> t = Lists.newArrayList();
            final ListMultimap<FlexProvider, Boolean> allocResults = ArrayListMultimap.create();

            for (final FlexProvider p : getProviders()) {
                for (final String s : allocDvarID.get(p)) {
                    t.add(concreteResult.getBoolean(s));
                    allocResults.put(p, concreteResult.getBoolean(s));
                }
            }
            this.results = AllocResults
                    .create(allocResults, concreteResult.getObjective().doubleValue());
        } else {
            this.results = AllocResults.INFEASIBLE;
        }
    }

    @Override
    public Solver getSolver() {
        return this.solver;
    }

    @Override
    public MpProblem getProblem() {
        final MpProblem prob = new MpProblem();
        addDataToProblem(prob);

        for (final FlexProvider f : getProviders()) {
            addDVarsForAllocationToProb(prob, f);
        }

        addSolvedClauseToProb(prob);
        addGoalToProb(prob);

        for (final FlexProvider f : getProviders()) {
            addConstraintsForFlexToProb(prob, f);
        }
        return prob;
    }

    private void addSolvedClauseToProb(final MpProblem prob) {
        //solvedID
        for (final String s : solvedID) {
            prob.addVar(s, Double.class);
        }
        //solvedIDConstraints1
        for (int i = 0; i < profile.length(); i++) {
            final MpExpr lhs = new MpExpr().add(solvedID.get(i));
            final MpExpr rhs = new MpExpr().add(profile.value(i));
            prob.addConstraint(new MpConstraint(lhs, MpOperator.LE, rhs));
        }
        //solvedIDConstraints2 = isMin0andActive
        for (int i = 0; i < profile.length(); i++) {
            final MpExpr lhs = new MpExpr().add(solvedID.get(i));
            final MpExpr rhs = new MpExpr();
            for (final FlexProvider p : getProviders()) {
                rhs.add(prod(prod(allocDvarID.get(p).get(i), p.getPowerRate()),
                        1 / STEPS_PER_HOUR));
            }
            prob.addConstraint(new MpConstraint(lhs, MpOperator.LE, rhs));
        }
    }

    private void addDataToProblem(final MpProblem prob) {
        //cong
        for (final String s : congID) {
            prob.addVar(s, Double.class);
        }
        //congConstraints
        for (int i = 0; i < profile.length(); i++) {
            final MpExpr lhs = new MpExpr().add(congID.get(i));
            final MpExpr rhs = new MpExpr().add(profile.value(i));
            prob.addConstraint(new MpConstraint(lhs, MpOperator.EQ, rhs));
        }
        //flexRateConstraints
        for (final FlexProvider f : getProviders()) {
            final String flexVar = getFlexID(f);
            flexID.put(f, flexVar);
            prob.addVar(flexVar, Double.class);
            final MpExpr lhs = new MpExpr().add(flexVar);
            final MpExpr rhs = new MpExpr().add(f.getPowerRate());
            prob.addConstraint(new MpConstraint(lhs, MpOperator.EQ, rhs));
        }
    }

    private void addGoalToProb(final MpProblem tempProb) {
        final MpExpr goalExpr = new MpExpr();
        for (int i = 0; i < profile.length(); i++) {
            goalExpr.add(solvedID.get(i));
        }
        tempProb.setObjective(goalExpr, MpDirection.MAX);
    }

    private void addDVarsForAllocationToProb(final MpProblem p, final FlexProvider pv) {
        for (int c = 0; c < profile.length(); c++) {
            final String alloc = ALLOC + pv.hashCode() + ":" + c;
            allocDvarID.put(pv, alloc);
            p.addVar(alloc, Boolean.class);
        }
    }

    private String getFlexID(final FlexProvider pv) {
        return FLEX + ":" + pv.hashCode();
    }

    private void addConstraintsForFlexToProb(final MpProblem p, final FlexProvider pv) {
        //flexConstraints
        final FlexConstraints adapted = new ConstraintStepMultiplierDecorator(
                pv.getActivationConstraints(),
                STEPS_PER_HOUR);
        final MpDsoAdapter adapt = new MpDsoAdapter(adapted, allocDvarID.get(pv));
        adapt.getConstraints().forEach(p::addConstraint);

    }

    /**
     * @return The results.
     */
    @Override
    public AllocResults getResults() {
        return this.results;
    }

}

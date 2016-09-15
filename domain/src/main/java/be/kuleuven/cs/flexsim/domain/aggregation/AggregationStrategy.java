package be.kuleuven.cs.flexsim.domain.aggregation;

import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.FlexTuple;
import com.google.common.collect.Multimap;

/**
 * A strategy for performing aggregation duties.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@FunctionalInterface
public interface AggregationStrategy {

    /**
     * Perform an aggregation step using the following.
     *
     * @param context the aggregators dispatch entity.
     * @param t       the current time.
     * @param flex    the flexibility mapping.
     * @param target  the target flexibility to gather.
     * @return the actually gathered flexibility.
     */
    int performAggregationStep(AggregationContext context, int t,
            Multimap<SiteFlexAPI, FlexTuple> flex, int target);

}

package be.kuleuven.cs.flexsim.domain.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

public class CollectionUtilsTest {

    List<FlexBid> bids = Lists.newArrayList();

    @Before
    public void setUp() throws Exception {
        bids.add(new FlexBid(FlexTuple.NONE, 1));
        bids.add(new FlexBid(FlexTuple.NONE, 2));
        bids.add(new FlexBid(FlexTuple.NONE, 4));
        bids.add(new FlexBid(FlexTuple.NONE, 3));
    }

    @Test
    public void testMax() {
        assertEquals(bids.get(2).getValuation(),
                CollectionUtils.max(bids, input -> input.getValuation()));
    }

    @Test(expected = NoSuchElementException.class)
    public void testMaxEmpty() {
        assertEquals(bids.get(2).getValuation(), CollectionUtils
                .max(new ArrayList<FlexBid>(), input -> input.getValuation()));
    }

    @Test
    public void testArgMax() {
        bids.add(new FlexBid(FlexTuple.NONE, 7));
        assertEquals(bids.get(4),
                CollectionUtils.argMax(bids, input -> input.getValuation()));
    }

    @Test
    public void testSum() {
        assertEquals(10,
                CollectionUtils.sum(bids, input -> input.getValuation()));
    }

    @Test
    public void testCorner() {
        bids = Lists.newArrayList();
        bids.add(new FlexBid(FlexTuple.NONE, 10));
        bids.add(new FlexBid(FlexTuple.NONE, 2));
        bids.add(new FlexBid(FlexTuple.NONE, 4));
        bids.add(new FlexBid(FlexTuple.NONE, 3));
        bids.add(new FlexBid(FlexTuple.NONE, 7));

        assertEquals(bids.get(0).getValuation(),
                CollectionUtils.max(bids, input -> input.getValuation()));

        assertEquals(bids.get(0),
                CollectionUtils.argMax(bids, input -> input.getValuation()));

        assertEquals(26,
                CollectionUtils.sum(bids, input -> input.getValuation()));
    }
}

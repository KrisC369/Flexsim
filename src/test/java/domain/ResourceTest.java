package domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ResourceTest {

    IResource res;

    @Before
    public void setUp() throws Exception {
        res = ResourceFactory.createResource(3, 1, 1, 2, 4);
    }

    @Test
    public void testProcessing() {
        assertEquals(3, res.getCurrentNeededProcessTime());
        res.process(3);
        res.notifyOfHasBeenBuffered();
        assertEquals(1, res.getCurrentNeededProcessTime());
        res.process(1);
        res.notifyOfHasBeenBuffered();
        assertEquals(1, res.getCurrentNeededProcessTime());
        res.process(1);
        res.notifyOfHasBeenBuffered();
        assertEquals(2, res.getCurrentNeededProcessTime());
        res.process(1);
        res.notifyOfHasBeenBuffered();
        assertEquals(4, res.getCurrentNeededProcessTime());
    }

    @Test
    public void testInitial() {
        assertEquals(3, res.getCurrentNeededProcessTime());
    }

}

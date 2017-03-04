package no.mesan.handterminator.util;

import junit.framework.TestCase;

import java.util.List;

import no.mesan.handterminator.Mockup;
import no.mesan.handterminator.model.Route;
import no.mesan.handterminator.model.db.Task;

public class RouteUtilTest extends TestCase {
    private Route route;

    public void setUp() throws Exception {
        super.setUp();
        route = new Mockup().generateTestRoute();

        //make sure we got objects from database
        assertNotSame(null, route.getTasks());
        assertTrue(route.getTasks().size() != 0);

    }

    public void tearDown() throws Exception {

    }

    public void testUpdateDirections() throws Exception {

    }

    public void testSetGoogleAddresses() throws Exception {

    }

    public void testEstimateDeliveries() throws Exception {

    }

    /**
     * Warning: this method does not output optimized list. It just sorts based on arbitrary numbers
     */
    public void testSortTasks() throws Exception {
        List<Task> oldTasks = route.getTasks();
        List<Integer> waypoint_order = route.getWaypointOrder();

        RouteUtil.sortTasks(route);
        List<Task> newTasks = route.getTasks();

        //we actually have waypoints
        assertTrue(waypoint_order.size() != 0);
        for(int i = 0; i < waypoint_order.size(); i++){
            //assert that all tasks have been placed in order
            assertEquals(oldTasks.get(waypoint_order.get(i)), newTasks.get(i));
        }


    }
}
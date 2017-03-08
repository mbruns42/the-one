package test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.*;
import movement.MovementModel;
import movement.Path;
import movement.map.MapNode;
import movement.map.PanicMovementUtil;
import movement.map.SimMap;
import movement.PanicMovement;
import core.Coord;
import core.DTNHost;
import core.Settings;

/** 
 * JUnit Tests for the class PanicMovement
 **/
public class PanicMovementTest extends TestCase {

	//										   n1       n2       n6        n3
	private static final String WKT = "LINESTRING (1.0 1.0, 2.0 1.0, 3.0 1.0, 4.0 1.0) \n" +
	//              n1        n4
	"LINESTRING (1.0 1.0, 1.0 2.0)\n"+
	//              n2       n7       n3       n6
	"LINESTRING (2.0 1.0, 2.0 0.0, 3.0 0.0, 3.0 1.0)\n";
	//
	
	private MapNode[] node = new MapNode[7];
	private MapNode event;

	private PanicMovement panicMovement;
	private SimMap map;
	private TestSettings settings;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		setupMapDataAndBasicSettings();
		panicMovement.setHost(setupHost());
	}
	
	/** This class tests the PanicMovement class using this
	 * topology:  n7--n5
	 *            |   |
	 *        n1--n2--n6--n3
	 *         |
	 *        n4
	 **/
	private void createTopology(MapNode[] node) {
		node[0].addNeighbor(node[1]);
		node[0].addNeighbor(node[3]);
		node[1].addNeighbor(node[0]);
		node[1].addNeighbor(node[5]);
		node[1].addNeighbor(node[6]);
		node[2].addNeighbor(node[5]);
		node[3].addNeighbor(node[0]);
		node[4].addNeighbor(node[5]);
		node[4].addNeighbor(node[6]);
		node[5].addNeighbor(node[1]);
		node[5].addNeighbor(node[2]);
		node[5].addNeighbor(node[4]);
		node[6].addNeighbor(node[1]);
		node[6].addNeighbor(node[4]);
	}
	
	/**
	 * Sets up the map described above, the panic movement and the event, as well as speed and wait time settings
	 */
	private void setupMapDataAndBasicSettings() {
		Settings.init(null);

		settings = new TestSettings();
		settings.putSetting(MovementModel.SPEED, "1,1");
		settings.putSetting(MovementModel.WAIT_TIME, "0,0");
		
		Coord[] coord = new Coord[7];
		
		coord[0] = new Coord(1,1);
		coord[1] = new Coord(2,1);
		coord[2] = new Coord(4,1);
		coord[3] = new Coord(1,2);
		coord[4] = new Coord(3,0);
		coord[5] = new Coord(3,1);
		coord[6] = new Coord(2,0);

		node[0] = new MapNode(coord[0]);
		node[1] = new MapNode(coord[1]);
		node[2] = new MapNode(coord[2]);
		node[3] = new MapNode(coord[3]);
		node[4] = new MapNode(coord[4]);
		node[5] = new MapNode(coord[5]);
		node[6] = new MapNode(coord[6]);

		Map<Coord, MapNode> cmMap = new HashMap<Coord, MapNode>();
		cmMap.put(coord[0], node[0]);
		cmMap.put(coord[1], node[1]);
		cmMap.put(coord[2], node[2]);
		cmMap.put(coord[3], node[3]);
		cmMap.put(coord[4], node[4]);
		cmMap.put(coord[5], node[5]);
		cmMap.put(coord[6], node[6]);

		map = new SimMap(cmMap);
		createTopology(node);
		event = map.getNodeByCoord(new Coord(2,1));
		panicMovement = new PanicMovement(settings, map, 3, event.getLocation(), 1.0, 1.5); 
	}

	/**
	 * Tests if the angle between source node, event and target node is between 90 and 270 degrees (so the 
	 * absolute of the difference of straight angle and this angle should be less or equal to 90 degrees)
	 * to avoid running through the event 
	 */
	public void testAngle() {
		
		Path path = panicMovement.getPath();
		MapNode start = map.getNodeByCoord(path.getCoords().get(0));
		MapNode end = map.getNodeByCoord(path.getCoords().get(path.getCoords().size() - 1));
		
		double angle = PanicMovementUtil.computeAngleBetween(event.getLocation(), start, end);
		assertTrue("Angle should be between 90 and 270 degrees", Math.abs(180 - angle)  <= 90);
	}
	
	/**
	 * Tests if the target node is inside the safe area
	 */
	public void testSafeRegion() {

		Path path = panicMovement.getPath();
		MapNode end = map.getNodeByCoord(path.getCoords().get(path.getCoords().size() - 1));
		
		assertTrue("Target node should be inside the safe area", 
				end.getLocation().distance(event.getLocation()) >= panicMovement.getPanicMovementUtil().getSafeRangeRadius() );
	}

	/**
	 * Test if closest possible node to the host is selected
	 */
	public void testOptimizationCriterion() {
		
		Path path = panicMovement.getPath();
		MapNode start = map.getNodeByCoord(path.getCoords().get(0));
		MapNode end = map.getNodeByCoord(path.getCoords().get(path.getCoords().size() - 1));
		
		for(MapNode m : node) {
			if (end.getLocation().distance(panicMovement.getHost().getLocation()) 
					> m.getLocation().distance(panicMovement.getHost().getLocation())) {
				assertTrue("Closest possible node to the host should be selected",
						m.getLocation().distance(event.getLocation()) < panicMovement.getPanicMovementUtil().getSafeRangeRadius()
						|| panicMovement.getPanicMovementUtil().isInEventDirection(start, m));
			}
		}
	}

	/**
	 * Tests if the target node is not outside the event range
	 */
	public void testEventRange() {

		Path path = panicMovement.getPath();
		MapNode end = map.getNodeByCoord(path.getCoords().get(path.getCoords().size() - 1));
		
		assertTrue("Target node should not be outside the event range",
				end.getLocation().distance(event.getLocation()) <= panicMovement.getPanicMovementUtil().getEventRangeRadius() );
	}
	
	/**
	 * Creates a host for the map
	 * @return created host
	 */
	private DTNHost setupHost() {
		TestUtils utils = new TestUtils(null, null, settings);
		DTNHost h1 = utils.createHost(panicMovement, null);

		h1.move(0); // get a path for the node
		// move node directly to first waypoint
		h1.setLocation(h1.getPath().getCoords().get(0));
		return h1;
	}
}

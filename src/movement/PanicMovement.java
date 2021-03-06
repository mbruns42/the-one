package movement;

import java.util.List;

import core.Coord;
import core.Settings;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.PanicMovementUtil;
import movement.map.SimMap;

/**
 * @author Nils Weidmann
 *         Implementation of the Panic Mobility Model. If an event occurs, all DTNHosts move towards the "Safe Zone"
 *         between the save range radius and the event range radius. Furthermore, they do not cross the event, such that
 *         the angle between them, the event and the target is at most 90°
 */
public class PanicMovement extends MapBasedMovement {

    private static final double DEFAULT_SAFE_RANGE = 1000.0;
    private static final double DEFAULT_EVENT_LOCATION_X = 1500.0;
    private static final double DEFAULT_EVENT_LOCATION_Y = 1500.0;

    private DijkstraPathFinder pathFinder;

    private Coord eventLocation = new Coord(DEFAULT_EVENT_LOCATION_X, DEFAULT_EVENT_LOCATION_Y);
    private double safeRange = DEFAULT_SAFE_RANGE;

    /**
     * Constructor setting values for the event and the minimum and maximum distance to
     * an event (SAFE_RANGE_RADIUS, EVENT_RANGE_RADIUS).
     *
     * @param settings Settings for the map, hosts etc.
     */
    public PanicMovement(Settings settings) {
        super(settings);
        pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
    }

    /**
     * Copyconstructor.
     *
     * @param pm The PanicMovement prototype to base
     *           the new object to
     */
    protected PanicMovement(PanicMovement pm) {
        super(pm);
        this.pathFinder = pm.pathFinder;
    }

    /**
     * Additional constructor for JUnit Tests
     *
     * @param settings         Settings for the map, hosts etc.
     * @param newMap           Map passed instead of reading it from a file
     * @param nrofMaps         Number of WKT files
     */
    public PanicMovement(Settings settings, SimMap newMap, int nrofMaps) {
        super(settings, newMap, nrofMaps);
        pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
    }

    /**
     * Determines a path to the most suitable node in the security zone
     */
    @Override
    public Path getPath() {
        Path path = new Path(generateSpeed());
        MapNode hostNode = getMap().getClosestNodeByCoord(getLastLocation());
        MapNode destNode = PanicMovementUtil.selectDestination(getMap(), hostNode, eventLocation, safeRange);

        List<MapNode> nodePath = pathFinder.getShortestPath(hostNode, destNode);

        for (MapNode node : nodePath) {
            // create a Path from the shortest path
            path.addWaypoint(node.getLocation());
        }

        lastMapNode = destNode;

        return path;
    }

    @Override
    public PanicMovement replicate() {
        return new PanicMovement(this);
    }

    /**
     * Get the location of the disaster that should be reacted to.
     * @return The location of the disaster that is being reacted to.
     */
    public Coord getEventLocation() {
        return eventLocation;
    }

    /**
     * Set the location of the disaster that should be reacted to.
     * @param eventLocation The location of the disaster.
     */
    public void setEventLocation(Coord eventLocation) {
        this.eventLocation = eventLocation;
    }

    /**
     * Get the distance from the disaster from which a host is safe.
     * @return The distance from the disaster from which a host is safe.
     */
    public double getSafeRange() {
        return safeRange;
    }

    /**
     * Set the distance from the disaster from which a host is safe.
     * @param safeRange The distance from the disaster from which a host is safe.
     */
    public void setSafeRange(double safeRange) {
        this.safeRange = safeRange;
    }
}

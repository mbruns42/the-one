package input;

import core.Coord;
import core.SimError;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.io.IOException;

/**
 * This is a container that includes all parameters of a VHMEvent
 *
 * Created by Marius Meyer on 15.02.17.
 */
public class VHMEvent extends ExternalEvent{

    /**
     * Minimum possible event intensity
     */
    public static final int MIN_INTENSITY = 1;

    /**
     * Maximum possible event intensity
     */
    public static final int MAX_INTENSITY = 10;


    /**
     * Default event intensity that is used if no other value is given
     */
    public static final int DEFAULT_INTENSITY = MIN_INTENSITY;

    /**
     * Default event maximum range that is used if no other value is given
     */
    public static final double DEFAULT_MAX_RANGE = Double.MAX_VALUE;

    /**
     * Default event end time that is used if no other value is given
     */
    public static final double DEFAULT_END_TIME = Double.MAX_VALUE;

    /**
     * Default event start time that is used if no other value is given
     */
    public static final double DEFAULT_START_TIME = 0.0;

    /**
     * Default event safe range that is used if no other value is given
     */
    public static final double DEFAULT_SAFE_RANGE = 0.0;

    public static final String EVENT_TYPE = "type";
    public static final String START_TIME = "start";
    public static final String END_TIME = "end";
    public static final String EVENT_LOCATION = "location";
    public static final String EVENT_LOCATION_X = "x";
    public static final String EVENT_LOCATION_Y = "y";
    public static final String EVENT_RANGE = "event_range";
    public static final String SAFE_RANGE = "safe_range";
    public static final String MAX_RANGE = "max_range";
    public static final String EVENT_INTENSITY = "intensity";


    /**
     * Type of a VHMEvent. This event type can be requested by a node
     */
    public enum VHMEventType{
        /**
         * A disaster where nodes will try to help or flee
         */
        DISASTER,

        /**
         * A hospital where nodes will transport victims to
         */
        HOSPITAL
    }

    /**
     * Static variable to store the event ID of the next created VHMEvent.
     * Do not access this directly because of synchronization issues.
     * Use {@link VHMEvent#getNextEventID()} instead.
     */
    private static long nextEventID = 0;

    /**
     * Name of an event
     */
    private String name;

    /**
     * Unique id of an event that is used to distinguish between events
     */
    private long id;

    /**
     *  The type of an event
     */
    private VHMEventType type;

    /**
     * The time point an event will start
     */
    private double startTime;

    /**
     * The time point an event will end
     */
    private double endTime;

    /**
     * The location of an event
     */
    private transient Coord location;

    /**
     * The range around an event's location, where nodes will be directly affected
     * by the event. This may be the area of a flooding or the buildings of a hospital.
     */
    private double eventRange;

    /**
     * The range around an event's location, where nodes are safe
     */
    private double safeRange;

    /**
     * The range around an event's location, where nodes will react to an event
     */
    private double maxRange;

    /**
     * The intensity of an event. This is an integer between {@link VHMEvent#MIN_INTENSITY}
     * and {@link VHMEvent#MAX_INTENSITY}
     */
    private int intensity;

    /**
     * Creates a new VHMEvent using a JSON object
     *
     * @param name The name of the event. This should be the key value of the JSON object
     * @param object The JSON object, that represents the event
     * @throws IOException If the JSON object could not be parsed to an VHMEvent
     */
    public VHMEvent(String name,JsonObject object) throws IOException{
        super(0);
        try {
            this.id = getNextEventID();
            //Parse mandatory parameters
            if (name != null) {
                this.name = name;
            } else throw new SimError("Event must have an identifier!");

            //parse event type
            this.type = VHMEventType.valueOf(((JsonString) object.get(EVENT_TYPE)).getString());

            //parse event location
            JsonObject loc = (JsonObject) object.get(EVENT_LOCATION);
            double x = ((JsonNumber)loc.get(EVENT_LOCATION_X)).doubleValue();
            double y = ((JsonNumber)loc.get(EVENT_LOCATION_Y)).doubleValue();
            this.location = new Coord(x,y);

            //parse event range
            this.eventRange = ((JsonNumber)object.get(EVENT_RANGE)).doubleValue();

            //parse safe range or use 0
            if (object.containsKey(SAFE_RANGE)){
                this.safeRange = ((JsonNumber)object.get(SAFE_RANGE)).doubleValue();
            }
            else{
                this.safeRange = DEFAULT_SAFE_RANGE;
            }

            //parse max range or take maximum distance as max range
            if (object.containsKey(MAX_RANGE)){
                this.maxRange = ((JsonNumber)object.get(MAX_RANGE)).doubleValue();
            }
            else{
                this.maxRange = DEFAULT_MAX_RANGE;

            }

            //parse start time or set default
            if (object.containsKey(START_TIME)){
                this.startTime = ((JsonNumber)object.get(START_TIME)).doubleValue();
            }
            else{
                this.startTime = DEFAULT_START_TIME;
            }

            //parse end time or set default
            if (object.containsKey(END_TIME)){
                this.endTime = ((JsonNumber)object.get(END_TIME)).doubleValue();
            }
            else{
                this.endTime = DEFAULT_END_TIME;
            }

            //parse intensity or set it to min intensity
            if (object.containsKey(EVENT_INTENSITY)){
                this.intensity = ((JsonNumber)object.get(EVENT_INTENSITY)).intValue();
                assert intensity >= MIN_INTENSITY && intensity <= MAX_INTENSITY : "Intensity must be integer between " + MIN_INTENSITY
                        + " and " + MAX_INTENSITY;
            }
            else{
                this.intensity = DEFAULT_INTENSITY;
            }
        }catch (Exception e){
            throw new IOException("VHMEvent could not be parsed from JSON: " + e.getMessage());
        }
    }

    /**
     * Copy constructor for a VHMEvent
     *
     * @param event The event that should be copied
     */
    public VHMEvent(VHMEvent event){
        super(0);
        this.name = event.name;
        this.id = event.id;
        this.type = event.type;
        this.startTime = event.startTime;
        this.endTime = event.endTime;
        this.location = event.location.clone();
        this.eventRange = event.eventRange;
        this.safeRange = event.safeRange;
        this.maxRange = event.maxRange;
        this.intensity = event.intensity;
    }

    /**
     * Returns the next unique event id.
     * This method should only be used in the event's constructor.
     *
     * @return next event id
     */
    private synchronized long getNextEventID(){
        return nextEventID++;
    }

    /**
     * Returns the event's type
     *
     * @return the event's {@link VHMEvent#type}
     */
    public VHMEventType getType() {
        return type;
    }

    /**
     * Returns the event's start time
     *
     * @return the event's {@link VHMEvent#startTime}
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * Returns the event's end time
     * @return the event's {@link VHMEvent#endTime}
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     *Returns the event's location
     * @return the event's {@link VHMEvent#location}
     */
    public Coord getLocation() {
        return location.clone();
    }

    /**
     * Return the event's event range
     * @return the event's {@link VHMEvent#eventRange}
     */
    public double getEventRange() {
        return eventRange;
    }

    /**
     * Return the event's safe range
     * @return the event's {@link VHMEvent#safeRange}
     */
    public double getSafeRange() {
        return safeRange;
    }

    /**
     * Return the event's maximum range
     * @return the event's {@link VHMEvent#maxRange}
     */
    public double getMaxRange() {
        return maxRange;
    }

    /**
     * Returns the intensity of an event.
     *
     * @return the event's {@link VHMEvent#intensity}
     */
    public int getIntensity() {
        return intensity;
    }

    /**
     * Returns the {@link VHMEvent#name} of the event that was specified in the JSON file.
     * @return the event name
     */
    public String getName(){
        return this.name;
    }

    /**
     * Returns the unique {@link VHMEvent#id} of the event
     *
     * @return the unique id
     */
    public long getID(){
        return this.id;
    }

}

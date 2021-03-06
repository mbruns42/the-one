package test;

import core.BroadcastMessage;
import core.Coord;
import core.DTNHost;
import core.DataMessage;
import core.DisasterData;
import core.Group;
import core.Message;
import core.MulticastMessage;
import core.SettingsError;
import core.SimClock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import routing.util.DeliveryPredictabilityStorage;
import util.Tuple;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Contains tests for the {@link routing.util.DeliveryPredictabilityStorage} class.
 *
 * Created by Britta Heymann on 19.05.2017.
 */
public class DeliveryPredictabilityStorageTest {
    /* Constants needed for delivery predictabilities that are used in most tests. */
    private static final double BETA = 0.25;
    private static final double GAMMA = 0.95;
    private static final double SUMMAND = 0.75;
    private static final double WINDOW_LENGTH = 1;

    /* Time span smaller than the window length */
    protected static final double SHORT_TIME_SPAN = 0.1;

    /* Further constants for exception checks. */
    private static final double BELOW_ZERO = -0.1;
    private static final double GREATER_THAN_ONE = 1.1;

    /* Times needed for a scenario test. */
    private static final double FIRST_MEETING_TIME = 4;
    private static final double SECOND_MEETING_TIME = 6;

    /* Acceptable delta for double equality checks. */
    private static final double DOUBLE_COMPARISON_DELTA = 0.0001;

    /* Time span that induces a lot of decay. */
    private static final int TIME_SPAN_FOR_LARGE_DECAY = 90;

    private static final String EXPECTED_DIFFERENT_PREDICTABILITY = "Expected different delivery predictability.";
    private static final String EXPECTED_EMPTY_STORAGE = "No delivery predictabilities should have been set.";

    /* Objects needed for tests. */
    private TestUtils testUtils;
    private SimClock clock;
    private DTNHost attachedHost;
    private DeliveryPredictabilityStorage dpStorage;

    public DeliveryPredictabilityStorageTest() {
        // Empty constructor for "Classes and enums with private members should hava a constructor" (S1258).
        // This is dealt with by the setUp method.
    }

    @Before
    public void setUp() {
        this.testUtils = new TestUtils(new ArrayList<>(), new ArrayList<>(), new TestSettings());
        this.clock = SimClock.getInstance();
        this.attachedHost = this.testUtils.createHost();
        this.dpStorage =
                createDeliveryPredictabilityStorage(BETA, GAMMA, SUMMAND, WINDOW_LENGTH, this.attachedHost);
    }

    @After
    public void cleanUp() {
        this.clock.setTime(0);
        Group.clearGroups();
    }

    @Test(expected = SettingsError.class)
    public void testConstructorThrowsForNegativeBeta() {
        createDeliveryPredictabilityStorage(BELOW_ZERO, GAMMA, SUMMAND, WINDOW_LENGTH, this.attachedHost);
    }

    @Test(expected = SettingsError.class)
    public void testConstructorThrowsForBetaGreaterOne() {
        createDeliveryPredictabilityStorage(GREATER_THAN_ONE, GAMMA, SUMMAND, WINDOW_LENGTH, this.attachedHost);
    }

    @Test(expected = SettingsError.class)
    public void testConstructorThrowsForNegativeGamma() {
        createDeliveryPredictabilityStorage(BETA, BELOW_ZERO, SUMMAND, WINDOW_LENGTH, this.attachedHost);
    }

    @Test(expected = SettingsError.class)
    public void testConstructorThrowsForGammaGreaterOne() {
        createDeliveryPredictabilityStorage(BETA, GREATER_THAN_ONE, SUMMAND, WINDOW_LENGTH, this.attachedHost);
    }

    @Test(expected = SettingsError.class)
    public void testConstructorThrowsForNegativeSummand() {
        createDeliveryPredictabilityStorage(BETA, GAMMA, BELOW_ZERO, WINDOW_LENGTH, this.attachedHost);
    }

    @Test(expected = SettingsError.class)
    public void testConstructorThrowsForSummandGreaterOne() {
        createDeliveryPredictabilityStorage(BETA, GAMMA, GREATER_THAN_ONE, WINDOW_LENGTH, this.attachedHost);
    }

    @Test(expected = SettingsError.class)
    public void testConstructorThrowsForZeroSecondsInTimeUnit() {
        createDeliveryPredictabilityStorage(BETA, GAMMA, SUMMAND, 0, this.attachedHost);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAttachedHostThrowsForMissingHost() {
        this.dpStorage.setAttachedHost(null);
    }

    @Test
    public void testGetBeta() {
        Assert.assertEquals(
                "Expected different beta to be returned.",
                BETA, this.dpStorage.getBeta(), DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void testGetGamma() {
        Assert.assertEquals(
                "Expected different gamme to be returned.",
                GAMMA, this.dpStorage.getGamma(), DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void testGetSummand() {
        Assert.assertEquals(
                "Expected different summand to be returned.",
                SUMMAND, this.dpStorage.getSummand(), DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void testGetWindowLength() {
        Assert.assertEquals(
                "Expected different number of seconds to be returned.",
                WINDOW_LENGTH, this.dpStorage.getWindowLength(), DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void testGetAttachedHostAddress() {
        Assert.assertEquals(
                "Expected different address to be returned.",
                this.attachedHost.getAddress(), this.dpStorage.getAttachedHostAddress());
    }

    @Test
    public void testCopyConstructor() {
        DeliveryPredictabilityStorage copy = new DeliveryPredictabilityStorage(this.dpStorage);
        Assert.assertEquals(
                "Expected different beta.", this.dpStorage.getBeta(), copy.getBeta(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals(
                "Expected different gamma.", this.dpStorage.getGamma(), copy.getGamma(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals(
                "Expected different summand.", this.dpStorage.getSummand(), copy.getSummand(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals(
                "Expected different window length.",
                this.dpStorage.getWindowLength(), copy.getWindowLength(), DOUBLE_COMPARISON_DELTA);
    }

    /**
     * Tests that updating delivery predictabilites works as expected by playing through a small scenario.
     */
    @Test
    public void testUpdateMethodsWork() {
        // Create additional hosts.
        DTNHost b = this.testUtils.createHost();
        DTNHost c = this.testUtils.createHost();
        DeliveryPredictabilityStorage bStorage =
                createDeliveryPredictabilityStorage(BETA, GAMMA, SUMMAND, WINDOW_LENGTH, b);
        DeliveryPredictabilityStorage cStorage =
                createDeliveryPredictabilityStorage(BETA, GAMMA, SUMMAND, WINDOW_LENGTH, c);

        // Check all hosts have empty delivery predictability storages in the beginning.
        Assert.assertTrue(EXPECTED_EMPTY_STORAGE, this.dpStorage.getKnownAddresses().isEmpty());
        Assert.assertTrue(EXPECTED_EMPTY_STORAGE, bStorage.getKnownAddresses().isEmpty());
        Assert.assertTrue(EXPECTED_EMPTY_STORAGE, cStorage.getKnownAddresses().isEmpty());

        // Let own host and B meet each other at a certain time.
        this.clock.setTime(FIRST_MEETING_TIME);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(this.dpStorage, bStorage);

        // Check their delivery predictabilities to each other.
        double expectedPredictability = SUMMAND;
        Assert.assertEquals(
                EXPECTED_DIFFERENT_PREDICTABILITY,
                expectedPredictability, this.dpStorage.getDeliveryPredictability(b), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals(
                EXPECTED_DIFFERENT_PREDICTABILITY,
                expectedPredictability, bStorage.getDeliveryPredictability(this.attachedHost), DOUBLE_COMPARISON_DELTA);

        // Let B and C meet each other at a later time.
        this.clock.setTime(SECOND_MEETING_TIME);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(bStorage, cStorage);

        // Check their delivery predictabilites to each other.
        Assert.assertEquals(
                EXPECTED_DIFFERENT_PREDICTABILITY,
                expectedPredictability, cStorage.getDeliveryPredictability(b), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals(
                EXPECTED_DIFFERENT_PREDICTABILITY,
                expectedPredictability, bStorage.getDeliveryPredictability(c), DOUBLE_COMPARISON_DELTA);

        // Check their delivery predictabilites to ownHost.
        double decayedPredictability = SUMMAND * Math.pow(GAMMA, SECOND_MEETING_TIME - FIRST_MEETING_TIME);
        Assert.assertEquals(
                EXPECTED_DIFFERENT_PREDICTABILITY,
                decayedPredictability, bStorage.getDeliveryPredictability(this.attachedHost), DOUBLE_COMPARISON_DELTA);
        double transitivePredictability = SUMMAND * decayedPredictability * BETA;
        Assert.assertEquals(
                EXPECTED_DIFFERENT_PREDICTABILITY,
                transitivePredictability,
                cStorage.getDeliveryPredictability(this.attachedHost),
                DOUBLE_COMPARISON_DELTA);
    }

    /**
     * Tests that delivery predictabilities are decayed automatically after the specified time window completes.
     */
    @Test
    public void testUpdateHappensAfterTimeWindowCompletes() {
        // Make sure a delivery predictability > 0 exists.
        DTNHost b = this.testUtils.createHost();
        DeliveryPredictabilityStorage bStorage =
                createDeliveryPredictabilityStorage(BETA, GAMMA, SUMMAND, WINDOW_LENGTH, b);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(this.dpStorage, bStorage);

        // Remember original delivery predictability.
        double originalDeliveryPredictability = this.dpStorage.getDeliveryPredictability(b);

        // Update shortly before time window ends.
        this.clock.setTime(WINDOW_LENGTH - SHORT_TIME_SPAN);
        this.dpStorage.update();

        // The update shouldn't have done anything.
        Assert.assertEquals("Delivery predictabilities should not have been decayed yet.",
                originalDeliveryPredictability, this.dpStorage.getDeliveryPredictability(b), DOUBLE_COMPARISON_DELTA);

        // Update again at end of time window.
        this.clock.setTime(WINDOW_LENGTH);
        this.dpStorage.update();
        // Now, the delivery predictability should have been changed.
        Assert.assertNotEquals("Delivery predictability should have been decayed.",
                originalDeliveryPredictability, this.dpStorage.getDeliveryPredictability(b), DOUBLE_COMPARISON_DELTA);
    }

    /**
     * Tests that delivery predictabilities are decayed correctly even if different types of updates (because of
     * completed time windows or updates) happen.
     */
    @Test
    public void testMixedTimeWindowUpdatesAndMeetingsWork() {
        // Make sure a delivery predictability > 0 exists.
        DTNHost b = this.testUtils.createHost();
        DeliveryPredictabilityStorage bStorage =
                createDeliveryPredictabilityStorage(BETA, GAMMA, SUMMAND, WINDOW_LENGTH, b);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(this.dpStorage, bStorage);

        // Complete time window and update.
        this.clock.setTime(WINDOW_LENGTH);
        this.dpStorage.update();

        // Check new value.
        double decayedPredictability = SUMMAND * Math.pow(GAMMA, WINDOW_LENGTH);
        Assert.assertEquals(EXPECTED_DIFFERENT_PREDICTABILITY,
                decayedPredictability, this.dpStorage.getDeliveryPredictability(b),
                DOUBLE_COMPARISON_DELTA);

        // Advance only a short time span and meet another host.
        this.clock.advance(SHORT_TIME_SPAN);
        DTNHost c = this.testUtils.createHost();
        DeliveryPredictabilityStorage cStorage =
                createDeliveryPredictabilityStorage(BETA, GAMMA, SUMMAND, WINDOW_LENGTH, c);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(this.dpStorage, cStorage);

        // Check new value.
        decayedPredictability = SUMMAND * Math.pow(GAMMA, WINDOW_LENGTH + SHORT_TIME_SPAN);
        Assert.assertEquals(EXPECTED_DIFFERENT_PREDICTABILITY,
                decayedPredictability, this.dpStorage.getDeliveryPredictability(b),
                DOUBLE_COMPARISON_DELTA);

        // Complete time window and update.
        this.clock.advance(WINDOW_LENGTH - SHORT_TIME_SPAN);
        this.dpStorage.update();

        // Check new value.
        decayedPredictability = SUMMAND * Math.pow(GAMMA, WINDOW_LENGTH + WINDOW_LENGTH);
        Assert.assertEquals(EXPECTED_DIFFERENT_PREDICTABILITY,
                decayedPredictability, this.dpStorage.getDeliveryPredictability(b),
                DOUBLE_COMPARISON_DELTA);
    }

    /**
     * Tests that tiny delivery predictabilities are set to zero when decayed.
     */
    @Test
    public void testTinyDeliveryPredictabilitiesAreSetToZero() {
        // Make sure recipient is known.
        DTNHost recipient = this.testUtils.createHost();
        DeliveryPredictabilityStorage recipientStorage = createDeliveryPredictabilityStorage(recipient);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(recipientStorage, this.dpStorage);

        // Advance clock s.t. decaying should result in a tiny delivery predictability.
        this.clock.advance(TIME_SPAN_FOR_LARGE_DECAY);

        // Update delivery predictabilities by letting our host meet another one.
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(
                this.dpStorage, createDeliveryPredictabilityStorage(this.testUtils.createHost()));

        // Check delivery predictability to recipient.
        Assert.assertEquals(
                "Delivery predictability should be zero.",
                0, this.dpStorage.getDeliveryPredictability(recipient), 0);
    }

    /**
     * Delivery predictability for unknown host should be 0.
     */
    @Test
    public void testGetDeliveryPredictabilityForUnknownHost() {
        Assert.assertEquals(
                EXPECTED_DIFFERENT_PREDICTABILITY,
                0, this.dpStorage.getDeliveryPredictability(this.testUtils.createHost()), DOUBLE_COMPARISON_DELTA);
    }

    /**
     * Tests that {@link DeliveryPredictabilityStorage#getDeliveryPredictability(Message)} returns the same for a
     * one-to-one message as {@link DeliveryPredictabilityStorage#getDeliveryPredictability(DTNHost)} does for its
     * recipient.
     */
    @Test
    public void testGetDeliveryPredictabilityForOneToOneMessage() {
        // Make sure recipient is known.
        DTNHost recipient = this.testUtils.createHost();
        DeliveryPredictabilityStorage recipientStorage = createDeliveryPredictabilityStorage(recipient);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(recipientStorage, this.dpStorage);

        // Check delivery predictability for message.
        Message oneToOneMessage = new Message(this.testUtils.createHost(), recipient, "M1", 0);
        Assert.assertEquals(
                "Delivery predictability of 1-to-1 message should equal the delivery predictability to the recipient",
                this.dpStorage.getDeliveryPredictability(recipient),
                this.dpStorage.getDeliveryPredictability(oneToOneMessage),
                DOUBLE_COMPARISON_DELTA);
    }

    /**
     * Tests that {@link DeliveryPredictabilityStorage#getDeliveryPredictability(Message)} returns the same for a
     * multicast as the maximum {@link DeliveryPredictabilityStorage#getDeliveryPredictability(DTNHost)} returns for any
     * of its remaining recipients.
     */
    @Test
    public void testGetDeliveryPredictabilityForMulticastMessages() {
        // Create group for the multicast message.
        DTNHost recipient = this.testUtils.createHost();
        DTNHost oftenMetRecipient = this.testUtils.createHost();
        Group group = Group.createGroup(1);
        group.addHost(recipient);
        group.addHost(oftenMetRecipient);
        group.addHost(this.attachedHost);

        // Make sure we know both hosts in the group, but know one better than the other.
        DeliveryPredictabilityStorage recipientStorage = createDeliveryPredictabilityStorage(recipient);
        DeliveryPredictabilityStorage oftenMetRecipientStorage = createDeliveryPredictabilityStorage(oftenMetRecipient);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(recipientStorage, this.dpStorage);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(oftenMetRecipientStorage, this.dpStorage);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(oftenMetRecipientStorage, this.dpStorage);
        Assert.assertTrue(
                "One host should be known better than the other.",
                this.dpStorage.getDeliveryPredictability(oftenMetRecipient)
                        > this.dpStorage.getDeliveryPredictability(recipient));

        // Check delivery predictability of a multicast message.
        Message multicast = new MulticastMessage(this.attachedHost, group, "M1", 0);
        Assert.assertEquals(
                "Multicast delivery predictability should equal the maximum remaining recipient delivery pred.",
                this.dpStorage.getDeliveryPredictability(oftenMetRecipient),
                this.dpStorage.getDeliveryPredictability(multicast),
                DOUBLE_COMPARISON_DELTA);

        // Add better known recipient to message path.
        multicast.addNodeOnPath(oftenMetRecipient);

        // Delivery predictability should have decreased.
        Assert.assertEquals(
                "Multicast delivery predictability should equal the maximum remaining recipient delivery pred.",
                this.dpStorage.getDeliveryPredictability(recipient),
                this.dpStorage.getDeliveryPredictability(multicast),
                DOUBLE_COMPARISON_DELTA);

        // Add other recipient to message path.
        multicast.addNodeOnPath(recipient);

        // Delivery predictability should now be 0.
        Assert.assertEquals("Multicast delivery predictability should be zero if message has all recipients on path.",
                0, this.dpStorage.getDeliveryPredictability(multicast), DOUBLE_COMPARISON_DELTA);
    }

    /**
     * Tests that {@link DeliveryPredictabilityStorage#getDeliveryPredictability(Message)} throws an
     * {@link IllegalArgumentException} if the given message argument is a {@link DataMessage}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetDeliveryPredictabilityThrowsForDataMessage() {
        DisasterData data = new DisasterData(DisasterData.DataType.MARKER, 0, SimClock.getTime(), new Coord(0, 0));
        Message dataMessage = new DataMessage(
                this.testUtils.createHost(), this.attachedHost, "M1", Collections.singleton(new Tuple<>(data, 1D)),0);
        this.dpStorage.getDeliveryPredictability(dataMessage);
    }

    /**
     * Tests that {@link DeliveryPredictabilityStorage#getDeliveryPredictability(Message)} throws an
     * {@link IllegalArgumentException} if the given message argument is a {@link BroadcastMessage}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetDeliveryPredictabilityThrowsForBroadcastMessage() {
        Message broadcast = new BroadcastMessage(this.testUtils.createHost(), "M1", 0);
        this.dpStorage.getDeliveryPredictability(broadcast);
    }

    /**
     * Tests that if two hosts meet, the addresses known by the {@link DeliveryPredictabilityStorage} are extended by
     * each other.
     */
    @Test
    public void testMetHostsAreAddedToKnownAddresses() {
        DTNHost neighbor = this.testUtils.createHost();
        DeliveryPredictabilityStorage neighborStorage = createDeliveryPredictabilityStorage(neighbor);
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(neighborStorage, this.dpStorage);
        Assert.assertTrue(
                "Met hosts should be added to known addresses.",
                this.dpStorage.getKnownAddresses().contains(neighbor.getAddress()));
        Assert.assertTrue(
                "Met hosts should be added to known addresses.",
                neighborStorage.getKnownAddresses().contains(this.attachedHost.getAddress()));
    }

    /**
     * Tests that hosts a connected host knows about are also added to the host we know.
     */
    @Test
    public void testKnownAddressesAreExtendedTransitivitely() {
        // Create neighbor knowing another host we know nothing about.
        DTNHost nonNeighbor = this.testUtils.createHost();
        DeliveryPredictabilityStorage nonNeighborStorage = createDeliveryPredictabilityStorage(nonNeighbor);
        DeliveryPredictabilityStorage neighborStorage =
                createDeliveryPredictabilityStorage(this.testUtils.createHost());
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(nonNeighborStorage, neighborStorage);

        // Meet the neighbor.
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(dpStorage, neighborStorage);

        // Check we know the other host, too.
        Assert.assertTrue(
                "Hosts known by connected hosts should be added to known addresses.",
                this.dpStorage.getKnownAddresses().contains(nonNeighbor.getAddress()));
    }

    /**
     * Tests that own address is never added to known addresses.
     */
    @Test
    public void testOwnAddressIsNotAddedToKnownAddresses() {
        // Create neighbor.
        DeliveryPredictabilityStorage neighborStorage =
                createDeliveryPredictabilityStorage(this.testUtils.createHost());

        // Meet the neighbor.
        DeliveryPredictabilityStorage.updatePredictabilitiesForBothHosts(this.dpStorage, neighborStorage);
        Assert.assertTrue(
                "Neighbor should know us.",
                neighborStorage.getKnownAddresses().contains(this.attachedHost.getAddress()));

        // Make sure our own address was not added to known hosts.
        Assert.assertFalse(
                "Own address should never be added to known hosts.",
                this.dpStorage.getKnownAddresses().contains(this.attachedHost.getAddress()));
    }

    /**
     * Creates a {@link DeliveryPredictabilityStorage} for the given host using this default values specified by this
     * test class for all parameters.
     * @param host The host to be attached to the storage.
     * @return The created {@link DeliveryPredictabilityStorage}.
     */
    private static DeliveryPredictabilityStorage createDeliveryPredictabilityStorage(DTNHost host) {
        return createDeliveryPredictabilityStorage(BETA, GAMMA, SUMMAND, WINDOW_LENGTH, host);
    }

    /**
     * Creates a {@link DeliveryPredictabilityStorage}.
     * @param beta Constant indicating the importance of transitivity updates.
     * @param gamma Constant that determines how fast delivery predictabilities decay.
     * @param summand Constant used in direct updates, also known as DP_init.
     * @param windowLength Constant describing how many seconds are in a time unit.
     * @param host The host to be attached to this storage.
     * @return The created {@link DeliveryPredictabilityStorage}.
     */
    private static DeliveryPredictabilityStorage createDeliveryPredictabilityStorage(
            double beta, double gamma, double summand, double windowLength, DTNHost host) {
        TestSettings settings = new TestSettings();
        settings.setNameSpace(DeliveryPredictabilityStorage.DELIVERY_PREDICTABILITY_STORAGE_NS);
        settings.putSetting(DeliveryPredictabilityStorage.BETA_S, Double.toString(beta));
        settings.putSetting(DeliveryPredictabilityStorage.GAMMA_S, Double.toString(gamma));
        settings.putSetting(DeliveryPredictabilityStorage.SUMMAND_S, Double.toString(summand));
        settings.putSetting(DeliveryPredictabilityStorage.WINDOW_LENGTH_S, Double.toString(windowLength));
        settings.restoreNameSpace();

        DeliveryPredictabilityStorage storage = new DeliveryPredictabilityStorage();
        storage.setAttachedHost(host);
        return storage;
    }
}

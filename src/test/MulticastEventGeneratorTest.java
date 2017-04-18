package test;

import core.DTNHost;
import core.Group;
import core.SettingsError;
import core.SimError;
import core.SimScenario;
import input.AbstractMessageEventGenerator;
import input.MessageEventGenerator;
import input.MulticastCreateEvent;
import input.MulticastEventGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Contains tests for the MulticastEventGenerator class
 *
 * Created by Marius Meyer on 10.03.17.
 */
public class MulticastEventGeneratorTest extends AbstractMessageEventGeneratorTest{


    private static final int MIN_GROUP_COUNT = 1;
    private static final int MAX_GROUP_COUNT = 10;
    private static final int MIN_GROUP_SIZE = 2;
    private static final int MAX_GROUP_SIZE = 10;
    private static final int MAX_HOST_RANGE = 10;
    private static final int INVALID_MAX_GROUP_SIZE = MAX_HOST_RANGE + 1;

    @Before
    @Override
    public void init(){
        super.init();
        SimScenario.reset();
        Group.clearGroups();
        DTNHost.reset();
        TestSettings.addSettingsToEnableSimScenario(this.settings);

        /*redefine host range settings because we need to check an invalid maximal group size.
        For this, we need to know the host range specified in the settings.
        */
        this.settings.putSetting(AbstractMessageEventGenerator.HOST_RANGE_S, "0,"+MAX_HOST_RANGE);

        /* Make sure we have enough hosts. */
        settings.setNameSpace(SimScenario.GROUP_NS);
        settings.putSetting(SimScenario.NROF_HOSTS_S, Integer.toString(MAX_HOST_RANGE));
        settings.restoreNameSpace();

        settings.putSetting(MulticastEventGenerator.GROUP_COUNT_RANGE_S,MIN_GROUP_COUNT+", "+MAX_GROUP_COUNT);
        settings.putSetting(MulticastEventGenerator.GROUP_SIZE_RANGE_S,MIN_GROUP_SIZE+", "+MAX_GROUP_SIZE);
    }

    @Test
    public void testNextEventCreatesMulticastMessages() {
        AbstractMessageEventGenerator generator = new MulticastEventGenerator(this.settings);
        for(int i = 0; i < AbstractMessageEventGeneratorTest.NR_TRIALS_IN_TEST; i++) {
            assertTrue(
                    "Event should have been the creation of a multicast message.",
                    generator.nextEvent() instanceof MulticastCreateEvent);
        }
    }

    @Test(expected = SettingsError.class)
    public void testMulticastEventGeneratorConstructorThrowsErrorIfSingleHostIsSpecified() {
        this.settings.putSetting(AbstractMessageEventGenerator.HOST_RANGE_S, "0,1");
        new MulticastEventGenerator(this.settings);
    }


    @Test
    public void testGroupSizesAndGroupCountOfGeneratedGroupsAreInSpecifiedRange(){
        AbstractMessageEventGenerator generator = new MulticastEventGenerator(this.settings);
        for(int i = 0; i < AbstractMessageEventGeneratorTest.NR_TRIALS_IN_TEST; i++) {
            generator.nextEvent();
            assertTrue("Group count must be in specified range",
                    Group.getGroups().length <= MAX_GROUP_COUNT
                            && Group.getGroups().length >= MIN_GROUP_COUNT);
            for (Group g : Group.getGroups()) {
                assertTrue("Group size should be in specified range",
                        g.getMembers().length <= MAX_GROUP_SIZE &&
                                g.getMembers().length >= MIN_GROUP_SIZE);
            }
        }
    }

    @Test(expected = SimError.class)
    public void testGeneratorThrowsExceptionWhenMaxGroupSizeIsGreaterThanHostAddressRange(){
        settings.putSetting(MulticastEventGenerator.GROUP_SIZE_RANGE_S,MIN_GROUP_SIZE+", "+
                INVALID_MAX_GROUP_SIZE);
        new MulticastEventGenerator(this.settings);
    }
    
    @Test
    public void testPriorities(){
        AbstractMessageEventGenerator generator = new MulticastEventGenerator(this.settings);
        MulticastCreateEvent event;
        for(int i = 0; i < AbstractMessageEventGeneratorTest.NR_TRIALS_IN_TEST; i++) {
            event = (MulticastCreateEvent) generator.nextEvent();
            assertTrue(event.getPriority() <= 10);
            assertTrue(event.getPriority() >= 1);
        }
    }
    
    @Test
    public void testDoubleTimeEventDiff(){
        this.settings.putSetting(AbstractMessageEventGenerator.MESSAGE_INTERVAL_S, "0.1,1");
        double time;
        AbstractMessageEventGenerator generator = new MulticastEventGenerator(this.settings);
        for(int i = 0; i < NR_TRIALS_IN_TEST; i++){
            time = generator.drawNextEventTimeDiff();
            assertTrue(time <= 1);
            assertTrue(time >= 0.1);
        }
    }

    /**
     * Gets the class name of the class to generate message events with.
     */
    @Override
    protected String getMessageEventGeneratorClassName() {
        return MulticastEventGenerator.class.toString();
    }


}

package test;

import input.BroadcastEventGenerator;
import input.MessageEventGenerator;
import org.junit.Before;

/**
 * Contains common initialization for both MessageEventGeneratorTest and BroadcastEventGeneratorTest.
 *
 * Created by Britta Heymann on 22.02.2017.
 */
public abstract class AbstractMessageEventGeneratorTest {
    /**
     * The number of times a test is repeated for tests that might be non-deterministic.
     */
    protected static final int NR_TRIALS_IN_TEST = 10;

    protected TestSettings settings = new TestSettings();

    @Before
    public void init() {
        this.settings.putSetting("Events.nrof", "1");

        this.settings.putSetting("class", this.getMessageEventGeneratorClassName());
        this.settings.putSetting(MessageEventGenerator.MESSAGE_INTERVAL_S, "1,2");
        this.settings.putSetting(MessageEventGenerator.MESSAGE_SIZE_S, "500k,1M");
        this.settings.putSetting(MessageEventGenerator.HOST_RANGE_S, "0,126");
        this.settings.putSetting(MessageEventGenerator.MESSAGE_ID_PREFIX_S, "M");
    }

    /**
     * Gets the class name of the class to generate message events with.
     */
    protected abstract String getMessageEventGeneratorClassName();
}


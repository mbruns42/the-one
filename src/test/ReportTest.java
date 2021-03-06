package test;

import core.SimScenario;
import report.DataSyncReport;
import report.Report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import ui.DTNSimUI;

import static org.junit.Assert.*;

/**
 * Includes unit tests for {@link report.Report} class.
 *
 * Created by melanie on 19.05.17.
 */
public class ReportTest {

    private static final String WRONG_MAXIMUM = "The maximum was computed or formatted incorrectly.";
    private static final String WRONG_MINIMUM = "The minimum was computed or formatted incorrectly.";

    private File outputFile;
    private Report report;

    public ReportTest(){
        // Empty constructor for "Classes and enums with private members should have a constructor" (S1258).
        // This is dealt with by the setUp method.
    }

    @Before
    public void setUp() throws IOException {
        // Set locale for periods instead of commas in doubles.
        java.util.Locale.setDefault(java.util.Locale.US);

        /* Try functions out on DataSyncReport since Report is abstract */
        final String reportName = "DataSyncReport";

        /* Add necessary settings for creating a temporary output file */
        TestSettings settings = new TestSettings();
        settings.putSetting(DTNSimUI.NROF_REPORT_S, "1");
        settings.putSetting(Report.REPORTDIR_SETTING, "test");
        settings.putSetting("Report.report1", reportName);
        this.outputFile = File.createTempFile("reportTest", ".tmp");
        settings.setNameSpace(reportName);
        settings.putSetting(Report.OUTPUT_SETTING, outputFile.getAbsolutePath());
        settings.restoreNameSpace();

        report = new DataSyncReport();
    }

    @Test
    public void testGetMaximumReturnsNaNForEmptyList(){
        List<Double> values = new ArrayList<>();
        assertEquals("For empty lists, NaN should be returned", "NaN", report.getMaximum(values));
    }

    @Test
    public void testGetMaximumReturnsMaximum(){
        final List<Double> values = Arrays.asList(3.0, -1.2, 100.0, -100.3, 0.07);
        assertEquals(WRONG_MAXIMUM, "100.0000", report.getMaximum(values));
        final List<Double> valuesWithNegativeMax = Arrays.asList(-3.0, -1.2, -100.0, -100.3, -0.07);
        assertEquals(WRONG_MAXIMUM, "-0.0700", report.getMaximum(valuesWithNegativeMax));
        final List<Double> valuesWithSameNumberTwice = Arrays.asList(30.0, -1.2, 100.0, 100.3, -0.07, 100.0);
        assertEquals(WRONG_MAXIMUM, "100.3000", report.getMaximum(valuesWithSameNumberTwice));
        final  List<Double> valuesWithMaxTwice = Arrays.asList(30.0, -1.2, 100.0, 100.3, -0.07, 100.3);
        assertEquals(WRONG_MAXIMUM, "100.3000", report.getMaximum(valuesWithMaxTwice));
    }

    @Test
    public void testGetMinimumReturnsNaNForEmptyList(){
        List<Double> values = new ArrayList<>();
        assertEquals("For empty lists, NaN should be returned", "NaN", report.getMinimum(values));
    }

    @Test
    public void testGetMinimumReturnsMinimum(){
        final List<Double> values = Arrays.asList(3.0, -1.2, 100.0, 100.3, 0.07);
        assertEquals(WRONG_MINIMUM, "-1.2000", report.getMinimum(values));
        final List<Double> valuesWithNegativeMin = Arrays.asList(-3.0, -1.2, -100.0, -100.3, -0.07);
        assertEquals(WRONG_MINIMUM, "-100.3000", report.getMinimum(valuesWithNegativeMin));
        final List<Double> valuesWithSameNumberTwice = Arrays.asList(30.0, -1.2, 100.0, 100.3, -0.07, 100.0);
        assertEquals(WRONG_MINIMUM, "-1.2000", report.getMinimum(valuesWithSameNumberTwice));
        final  List<Double> valuesWithMinTwice = Arrays.asList(30.0, 1.2, 100.0, 100.3, 0.07, 100.3, 0.07);
        assertEquals(WRONG_MINIMUM, "0.0700", report.getMinimum(valuesWithMinTwice));
    }

    @After
    public void cleanUp() {
        SimScenario.reset();
        outputFile.delete();
    }
}

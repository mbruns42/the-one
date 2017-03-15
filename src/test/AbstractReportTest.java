package test;

import org.junit.After;
import org.junit.Before;
import report.Report;
import ui.DTNSimUI;

import java.io.File;
import java.io.IOException;

/**
 * A base class for all tests testing reports.
 *
 * Created by Britta Heymann on 08.03.2017.
 */
public abstract class AbstractReportTest {
    protected File outputFile;
    protected TestSettings settings;

    /**
     * Sets up a report using a temporary file to write to.
     * @throws IOException
     */
    @Before
    public void setUp() throws IOException {
        this.outputFile = File.createTempFile("reportTest", ".tmp");

        String reportName = this.getReportClass().getSimpleName();

        settings = new TestSettings();
        settings.putSetting(DTNSimUI.NROF_REPORT_S, "1");
        settings.putSetting(Report.REPORTDIR_SETTING, "test");
        settings.putSetting("Report.report1", reportName);
        settings.setNameSpace(reportName);
        settings.putSetting(Report.OUTPUT_SETTING, outputFile.getAbsolutePath());
        settings.restoreNameSpace();
    }

    @After
    public void deleteFile() {
        this.outputFile.delete();
    }

    /***
     * Gets the report class to test.
     * @return The the report class to test.
     */
    protected abstract Class getReportClass();
}

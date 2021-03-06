package tests.wurstscript.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstio.ErrorReportingIO;
import de.peeeq.wurstscript.ErrorReporting;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ErrorReportingTest {
    ErrorReporting instance = new ErrorReportingIO();

    @Ignore // would have side effects on server
    @Test
    public void testSendErrorReport() {
        boolean result = instance.sendErrorReport(new Error("bla"), "source");
        assertEquals(true, result);
    }

    @Ignore // would have side effects on server
    @Test
    public void testBigSource() throws IOException {
        String source = Files.toString(new File("/home/peter/kram/errorreport_source.wurst"), Charsets.UTF_8);

        boolean result = instance.sendErrorReport(new Error("bla"), source);
        assertEquals(true, result);
    }


}

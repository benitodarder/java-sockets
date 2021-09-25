package local.tin.tests.tcp.monitor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author benitodarder
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({InputStreamReader.class, BufferedReader.class, StreamsGenerator.class, PrintWriter.class, Socket.class})
public class StreamsGeneratorTest {

    private static final int SAMPLE_PORT = 666;
    private static final String SAMPLE_HOST = "host";

    @Test
    public void getBufferedReader_returns_expected_reader() throws Exception {
        InputStream mockedInputStream = mock(InputStream.class);
        InputStreamReader mockedInputStreamReader = mock(InputStreamReader.class);
        PowerMockito.whenNew(InputStreamReader.class).withArguments(mockedInputStream).thenReturn(mockedInputStreamReader);
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        PowerMockito.whenNew(BufferedReader.class).withArguments(mockedInputStreamReader).thenReturn(mockedBufferedReader);

        BufferedReader bufferedReader = StreamsGenerator.getInstance().getBufferedReader(mockedInputStream);

        assertEquals(mockedBufferedReader, bufferedReader);
    }

    @Test
    public void getPrintWriter_returns_expected_writer() throws Exception {
        OutputStream mockedOutputStream = mock(OutputStream.class);
        PrintWriter mockedPrintWriter = mock(PrintWriter.class);
        PowerMockito.whenNew(PrintWriter.class).withArguments(mockedOutputStream, true).thenReturn(mockedPrintWriter);

        PrintWriter printWriter = StreamsGenerator.getInstance().getPrintWriter(mockedOutputStream);

        assertEquals(mockedPrintWriter, printWriter);
    }

    @Test
    public void getSocket_returns_expected_value() throws Exception {
        Socket mockedSocket = mock(Socket.class);
        PowerMockito.whenNew(Socket.class).withArguments(SAMPLE_HOST, SAMPLE_PORT).thenReturn(mockedSocket);

        Socket socket = StreamsGenerator.getInstance().getSocket(SAMPLE_HOST, SAMPLE_PORT);

        assertEquals(mockedSocket, socket);
    }

}

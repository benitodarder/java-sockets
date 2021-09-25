package local.tin.tests.tcp.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.internal.verification.AtLeast;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author benitodarder
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({StreamsGenerator.class, Logger.class})
public class HttpConnectionTest {

    private static final String OUTPUT_STRING = "- Start message --------------------------------------------" + System.lineSeparator() 
            + "- Request --------------------------------------------------" + System.lineSeparator()
            + "A\n" + System.lineSeparator()
            + "\r\n"
            + "- Response -------------------------------------------------" + System.lineSeparator()
            + "B" + System.lineSeparator()
            + "- End message ----------------------------------------------" + System.lineSeparator();
    private static final String DESTINATION_HOST = "Host";
    private static final int DESTINATION_PORT = 6066;
    private static StreamsGenerator mockedStreamsGenerator;
    private static Logger mockedLogger;
    private HttpConnection httpConnection;
    private Socket mockedListenerSocket;
    private OutputStream mockedListenerOutputStream;
    private BufferedReader mockedListenerBufferedReader;
    private InputStream mockedListenerInputStream;
    private PrintWriter mockedListenerPrintWriter;
    private Socket mockedDestinationSocket;
    private OutputStream mockedDestinaionOutputStream;
    private BufferedReader mockedDestinationBufferedReader;
    private InputStream mockedDestinationInputStream;
    private PrintWriter mockedDestinationPrintWriter;

    @BeforeClass
    public static void setUpClasss() {
        mockedStreamsGenerator = mock(StreamsGenerator.class);
        mockedLogger = mock(Logger.class);
    }

    @Before
    public void setUp() throws IOException {
        PowerMockito.mockStatic(StreamsGenerator.class);
        when(StreamsGenerator.getInstance()).thenReturn(mockedStreamsGenerator);
        reset(mockedStreamsGenerator);
        PowerMockito.mockStatic(Logger.class);
        when(Logger.getLogger(HttpConnection.class)).thenReturn(mockedLogger);
        mockedListenerSocket = mock(Socket.class);
        mockedListenerOutputStream = mock(OutputStream.class);
        when(mockedListenerSocket.getOutputStream()).thenReturn(mockedListenerOutputStream);
        mockedListenerPrintWriter = mock(PrintWriter.class);
        when(mockedStreamsGenerator.getPrintWriter(mockedListenerOutputStream)).thenReturn(mockedListenerPrintWriter);
        mockedListenerBufferedReader = mock(BufferedReader.class);
        mockedListenerInputStream = mock(InputStream.class);
        when(mockedListenerSocket.getInputStream()).thenReturn(mockedListenerInputStream);
        when(mockedStreamsGenerator.getBufferedReader(mockedListenerInputStream)).thenReturn(mockedListenerBufferedReader);
        mockedDestinationSocket = mock(Socket.class);
        mockedDestinaionOutputStream = mock(OutputStream.class);
        when(mockedDestinationSocket.getOutputStream()).thenReturn(mockedDestinaionOutputStream);
        mockedDestinationPrintWriter = mock(PrintWriter.class);
        when(mockedStreamsGenerator.getPrintWriter(mockedDestinaionOutputStream)).thenReturn(mockedDestinationPrintWriter);
        mockedDestinationBufferedReader = mock(BufferedReader.class);
        mockedDestinationInputStream = mock(InputStream.class);
        when(mockedDestinationSocket.getInputStream()).thenReturn(mockedDestinationInputStream);
        when(mockedStreamsGenerator.getBufferedReader(mockedDestinationInputStream)).thenReturn(mockedDestinationBufferedReader);
        when(mockedStreamsGenerator.getSocket(DESTINATION_HOST, DESTINATION_PORT)).thenReturn(mockedDestinationSocket);
        httpConnection = new HttpConnection(mockedListenerSocket, DESTINATION_HOST, DESTINATION_PORT);
        when(mockedListenerBufferedReader.read()).thenReturn((int) 'A', (int) HttpConnection.NL, (int) HttpConnection.CR, (int) HttpConnection.NL);
        when(mockedDestinationBufferedReader.read()).thenReturn((int) 'B');
        when(mockedDestinationBufferedReader.ready()).thenReturn(true, false);
    }

    @Test
    public void run_gets_expected_destination_socket() throws IOException {

        httpConnection.run();

        verify(mockedStreamsGenerator).getSocket(DESTINATION_HOST, DESTINATION_PORT);
    }

    @Test
    public void run_reads_from_listener_socket() throws IOException {

        httpConnection.run();

        verify(mockedListenerBufferedReader, times(4)).read();
    }

    @Test
    public void run_writes_listened_string_into_destination() {

        httpConnection.run();

        verify(mockedDestinationPrintWriter).write("A" + HttpConnection.NL + HttpConnection.CR + HttpConnection.NL);
    }

    @Test
    public void run_reads_from_destination_socket() throws IOException {

        httpConnection.run();

        verify(mockedDestinationBufferedReader, times(1)).read();
    }

    @Test
    public void run_writes_response_string_into_listener() {

        httpConnection.run();

        verify(mockedListenerPrintWriter).write("B");
    }

    @Test
    public void run_logs_info_expected_string() {
        
        httpConnection.run();
        
        verify(mockedLogger, atLeast(1)).info(OUTPUT_STRING);
    }
}

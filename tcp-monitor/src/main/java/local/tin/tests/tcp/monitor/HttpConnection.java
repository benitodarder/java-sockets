package local.tin.tests.tcp.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author benitodarder
 */
public class HttpConnection implements Runnable {

    public static final int UNMATCHED_CONTENT_LENGTH = -1;
    public static final char CR = 0x0d;
    public static final char NL = 0x0a;
    public static final String HEADERS_END = "" + CR + NL;
    public static final String CONTENT_TYPE_REGEX = "^content-length:\\s*([\\d]+)$";
    private static final Logger LOGGER = Logger.getLogger(HttpConnection.class);
    private final Socket listenerSocket;
    private final String destinationHost;
    private final int destinationPort;

    public HttpConnection(Socket listenerSocket, String destinationHost, int destinationPort) {
        this.listenerSocket = listenerSocket;
        this.destinationHost = destinationHost;
        this.destinationPort = destinationPort;
    }

    @Override
    public void run() {

        try (Socket destinationSocket = StreamsGenerator.getInstance().getSocket(destinationHost, destinationPort)) {

            String requestString = getRequestString(listenerSocket.getInputStream());
            writeStringIntoStream(destinationSocket.getOutputStream(), requestString);
            String responseString = getResponseString(destinationSocket);
            writeStringIntoStream(listenerSocket.getOutputStream(), responseString);
            LOGGER.info(getLogMessage(requestString, responseString));
        } catch (IOException ex) {
            LOGGER.error("Unexpected IOException!", ex);
        } finally {
            try {
                listenerSocket.close();
            } catch (IOException ex) {
                LOGGER.error("Listening socket stubornely decided to throw an unexpected IOException on closing!", ex);
            }
        }
    }

    private void writeStringIntoStream(OutputStream outputStream, String requestString) throws IOException {
        PrintWriter destinationWriter = StreamsGenerator.getInstance().getPrintWriter(outputStream);
        destinationWriter.write(requestString);
        destinationWriter.flush();
    }

    private String getLogMessage(String requestString, String responseString) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("- Start message --------------------------------------------").append(System.lineSeparator());
        stringBuilder.append("- Request --------------------------------------------------").append(System.lineSeparator());
        stringBuilder.append(requestString).append(System.lineSeparator());
        stringBuilder.append("- Response -------------------------------------------------").append(System.lineSeparator());
        stringBuilder.append(responseString).append(System.lineSeparator());
        stringBuilder.append("- End message ----------------------------------------------").append(System.lineSeparator());
        return stringBuilder.toString();
    }

    private String getResponseString(final Socket destinationSocket) throws IOException {
        StringBuilder responseStringBuilder = new StringBuilder();
        do {
            BufferedReader destinationReader = StreamsGenerator.getInstance().getBufferedReader(destinationSocket.getInputStream());
            while (destinationReader.ready()) {
                responseStringBuilder.append((char) destinationReader.read());
            }
        } while (responseStringBuilder.toString().isEmpty());
        return responseStringBuilder.toString();
    }

    private String getRequestString(InputStream inputStream) throws IOException {
        BufferedReader listeningReader = StreamsGenerator.getInstance().getBufferedReader(inputStream);
        boolean headersRead = false;
        StringBuilder requestStringBuilder = new StringBuilder();
        while (!headersRead) {
            requestStringBuilder.append((char) listeningReader.read());
            if (isHeadersEnd(requestStringBuilder)) {
                headersRead = true;
            }
        }
        int contentLength = getContentLengthIfPresent(requestStringBuilder);
        int bytesRead = 0;
        while (bytesRead < contentLength) {
            requestStringBuilder.append((char) listeningReader.read());
            bytesRead++;
        }
        return requestStringBuilder.toString();
    }

    private boolean isHeadersEnd(StringBuilder stringBuilder) {
        return stringBuilder.length() > 3 && stringBuilder.substring(stringBuilder.length() - 3).equals(NL + HEADERS_END);
    }

    private int getContentLengthIfPresent(StringBuilder stringBuilder) {
        String[] messageByLines = stringBuilder.toString().split(HEADERS_END);
        Pattern pattern = Pattern.compile(CONTENT_TYPE_REGEX, Pattern.DOTALL);
        for (String current : messageByLines) {
            Matcher matcher = pattern.matcher(current.toLowerCase());
            if (matcher.matches()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return UNMATCHED_CONTENT_LENGTH;
    }

}

package local.tin.tests.tcp.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author benitodarder
 */
public class HttpConnection implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(HttpConnection.class);
    private static final int UNMATCHED_CONTENT_LENGTH = -1;
    private static final char CR = 0x0d;
    private static final char NL = 0x0a;
    private static final String HEADERS_END = "" + CR + NL;
    private static final String CONTENT_TYPE_REGEX = "^content-length:\\s*([\\d]+)$";
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

        try (Socket destinationSocket = new Socket(destinationHost, destinationPort);) {
            BufferedReader listeningReader = new BufferedReader(new InputStreamReader(listenerSocket.getInputStream()));
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
            PrintWriter destinationWriter = new PrintWriter(destinationSocket.getOutputStream(), true);
            destinationWriter.write(requestStringBuilder.toString());
            destinationWriter.flush();

            StringBuilder responseStringBuilder = new StringBuilder();
            do {
                BufferedReader destinationReader = new BufferedReader(new InputStreamReader(destinationSocket.getInputStream()));
                while (destinationReader.ready()) {
                    responseStringBuilder.append((char) destinationReader.read());
                }
            } while (responseStringBuilder.toString().isEmpty());
            PrintWriter listeningWriter = new PrintWriter(listenerSocket.getOutputStream(), true);
            listeningWriter.write(responseStringBuilder.toString());
            listeningWriter.flush();
            LOGGER.info("- Start message --------------------------------------------");
            LOGGER.info("- Request --------------------------------------------------");
            LOGGER.info(requestStringBuilder.toString());
            LOGGER.info("- Response -------------------------------------------------");
            LOGGER.info(responseStringBuilder.toString());
            LOGGER.info("- End message ----------------------------------------------");
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

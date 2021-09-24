package local.tin.tests.tcp.monitor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author benitodarder
 */
public class HttpMonitor {

    private static final Logger LOGGER = Logger.getLogger(HttpMonitor.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            LOGGER.error("Usage: java -jar echo-client...jar <listener port> <destination host> <destination port>");
        } else {

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    LOGGER.info("That's all folks!");
                }
            });
            try (ServerSocket listeningServerSocket = new ServerSocket(Integer.parseInt(args[0]));) {
                while (true) {
                    Socket listeningSocket = listeningServerSocket.accept();
                    LOGGER.debug("Connection accepted from: " + listeningSocket.getRemoteSocketAddress());
                    Thread thread = new Thread(new HttpConnection(listeningSocket, args[1], Integer.parseInt(args[2])));
                    thread.start();
                }
            }
        }
    }

}

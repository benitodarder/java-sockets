package local.tin.tests.tcp.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author benitodarder
 */
public class StreamsGenerator {

    private StreamsGenerator() {
    }

    public static StreamsGenerator getInstance() {
        return StreamsGeneratorHolder.INSTANCE;
    }

    private static class StreamsGeneratorHolder {
        private static final StreamsGenerator INSTANCE = new StreamsGenerator();
    }
    
    public BufferedReader getBufferedReader(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream));
    }
    
    public PrintWriter getPrintWriter(OutputStream outputStream) {
        return new PrintWriter(outputStream, true);
    }
    
    public Socket getSocket(String host, int port) throws IOException  {
        return new Socket(host, port);
    }
 }

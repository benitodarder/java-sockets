/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package local.tin.tests.echo.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author benitodarder
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            LOGGER.error("Usage: java -jar echo-server...jar <port number> <buffer size>");
        } else {

            int listeningPort = Integer.parseInt(args[0]);
            int listeningBufferSize = Integer.parseInt(args[1]);
            LOGGER.info("Waiting for connection...CTRL + C to exit.");
            try (
                    ServerSocket listeningServerSocket = new ServerSocket(Integer.parseInt(args[0]));
                    Socket listeningSocket = listeningServerSocket.accept();
                    PrintWriter listeningWriter = new PrintWriter(listeningSocket.getOutputStream(), true);
                    BufferedReader listeningReader = new BufferedReader(new InputStreamReader(listeningSocket.getInputStream()));) {
                LOGGER.info("Waiting for incoming messages...CTRL + C to exit.");
                char[] inputArray = new char[listeningBufferSize];
                while (listeningReader.read(inputArray) != -1) {
                    listeningWriter.write(inputArray);
                    listeningWriter.flush();
                    LOGGER.info("Received: " + new String(inputArray));
                    inputArray = new char[listeningBufferSize];
                }
            } catch (IOException e) {
                LOGGER.error("Exception caught when trying to listen on port " + listeningPort + " or listening for a connection", e);

            }
        }
    }

}

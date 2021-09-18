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
package local.tin.tests.echo.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author benitodarder
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length != 3) {
            LOGGER.error("Usage: java -jar echo-client...jar <host name> <port number> <buffer size>");
        } else {

            String serviceHost = args[0];
            int servicerPort = Integer.parseInt(args[1]);
            int serviceBufferSize = Integer.parseInt(args[2]);
            LOGGER.info("Connecting...");
            try (Socket serviceScoket = new Socket(serviceHost, servicerPort);
                    PrintWriter serviceWriter = new PrintWriter(serviceScoket.getOutputStream(), true);
                    BufferedReader serviceInput = new BufferedReader(new InputStreamReader(serviceScoket.getInputStream()));
                    BufferedReader dataInput = new BufferedReader(new InputStreamReader(System.in))) {
                LOGGER.info("Connected... Please type any message and press enter...CTRL + C to exit.");
                char[] inputArray = new char[serviceBufferSize];
                char[] echoedArray = new char[serviceBufferSize];
                while (dataInput.read(inputArray) != -1) {
                    serviceWriter.write(inputArray);
                    serviceWriter.flush();
                    serviceInput.read(echoedArray);
                    LOGGER.info("Echoed: " + new String(echoedArray));
                    LOGGER.info("Type any message and press enter...");
                    inputArray = new char[serviceBufferSize];
                    echoedArray = new char[serviceBufferSize];
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
    }

}

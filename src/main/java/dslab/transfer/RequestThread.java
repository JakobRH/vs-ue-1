package dslab.transfer;

import dslab.util.Config;
import dslab.util.DmtpMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

/**
 * Represents a RequestThread to handle a client connection of the transferserver. When receiving correct dmtp
 * commands, a MessageforwardingThread will be started to forward the received data.
 */
public class RequestThread extends Thread {

    private ExecutorService messageForwardingExecutorService;
    private Socket socket;
    private DmtpMessage dmtpMessage = new DmtpMessage();
    private Config config;

    /**
     * creates new RequestThread
     *
     * @param socket                           socket on which the connection to the client is based
     * @param messageForwardingExecutorService executorservice to handle the threads of messageforwarding
     * @param config                           config to pass on messageforwarding threads
     */
    public RequestThread(Socket socket, ExecutorService messageForwardingExecutorService, Config config) {
        this.messageForwardingExecutorService = messageForwardingExecutorService;
        this.socket = socket;
        this.config = config;
    }


    /**
     * Exchanges messages with the client via dmtp.
     */
    public void run() {
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            //first response to signal that dmtp is accepted
            writer.println("ok DMTP");
            writer.flush();

            String request;
            boolean beginFlag = false; //flag ot see if dmtp was started properly with the "begin" command

            //waiting for new request
            while (!this.isInterrupted()) {
                request = reader.readLine();
                if (request == null) {
                    break;
                }
                if (request.equals("")) {
                    continue;
                }
                String response;

                //if the starts of the dmtp exchange starts wrong (not with "begin")
                //error message will be sent to client and closes connection
                //break leads to finally block which closes connection
                if (!beginFlag && !request.equals("begin")) {
                    writer.println("error wrong start of dmtp");
                    break;
                }

                if (request.equals("begin")) {
                    beginFlag = true;
                    writer.println("ok");
                    writer.flush();
                    continue;
                }

                //when send command is received, the dmtp object will be checked
                //if dmtp object is correct a new thread will be started to handle the messageforwarding
                //otherwise responds with error message and continues to wait for new request
                if (request.equals("send")) {
                    String dmtpStatus = dmtpMessage.isValidToSend();
                    if (!dmtpStatus.equals("sendable")) {
                        writer.println(dmtpStatus);
                        writer.flush();
                        continue;
                    }
                    writer.println("ok");
                    writer.flush();
                    messageForwardingExecutorService.submit(new MessageForwardingThread(dmtpMessage, config));
                    continue;
                }

                //if quit command is received the connection will be closed
                if (request.equals("quit")) {
                    writer.println("ok bye");
                    writer.flush();
                    break;
                }

                response = checkRequest(request);

                // print request
                writer.println(response);
                writer.flush();

                //if checkRequest() returns error message connection will be closed
                if (response.equals("error  protocol  error")) {
                    break;
                }
            }

        } catch (SocketException e) {
            System.out.println("SocketException while handling socket: " + e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignored because we cannot handle it
                }
            }

        }


    }

    /**
     * checks if the request has valid data for dmtp, if wrong command is received error message will be returned
     * if the request is valid, the data will be added to the dmtp object
     *
     * @param request request
     * @return appropriate response to the received request
     */
    private String checkRequest(String request) {
        String[] splitRequest = request.split(" ", 2);

        if (splitRequest[0].equals("to")) {
            dmtpMessage.setTo(splitRequest[1]);
            return "ok " + splitRequest[1].split(",").length;
        }
        if (splitRequest[0].equals("from")) {
            dmtpMessage.setFrom(splitRequest[1]);
            return "ok";
        }
        if (splitRequest[0].equals("subject")) {
            dmtpMessage.setSubject(splitRequest[1]);
            return "ok";
        }
        if (splitRequest[0].equals("data")) {
            dmtpMessage.setData(splitRequest[1]);
            return "ok";
        }
        return "error  protocol  error";
    }
}

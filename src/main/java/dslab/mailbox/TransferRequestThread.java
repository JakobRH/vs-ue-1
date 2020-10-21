package dslab.mailbox;

import dslab.util.DmtpMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Represents a TransferRequestThread. Aim of this thread is to handle the serverside of a dmtp communication.
 */
public class TransferRequestThread extends Thread {

    private UserData userData;
    private Socket socket;
    private DmtpMessage dmtpMessage = new DmtpMessage();

    /**
     * Creates new instance of TransferRequestThread.
     * @param socket the socket to listen to.
     * @param userData the data structure of this server.
     */
    public TransferRequestThread(Socket socket, UserData userData) {
        this.socket = socket;
        this.userData = userData;
    }

    /**
     * Handles the dmtp communication of the client connection.
     */
    public void run() {
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            //first response to signal that dmtp is accepted
            writer.println("ok DMTP");
            writer.flush();

            String request;
            boolean beginFlag = false;

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
                    writer.flush();
                    break;
                }

                if (request.equals("begin")) {
                    beginFlag = true;
                    writer.println("ok");
                    writer.flush();
                    continue;
                }

                //if there is a address in the to-field that is not known on this server an error message will be
                //sent to the client.
                if (request.startsWith("to")) {
                    dmtpMessage.setTo(request.split(" ", 2)[1]);
                    ArrayList<String> unknownRecipients = userAvailable();
                    //should be empty if all addresses of the to-field are known on this server
                    if (!unknownRecipients.isEmpty()) {
                        writer.println("error unknown  recipient " + String.join(" ", unknownRecipients));
                        writer.flush();
                        dmtpMessage.setTo(null);
                        continue;
                    }
                    writer.println("ok " + dmtpMessage.getTo().split(",").length);
                    writer.flush();
                    continue;
                }

                //checks if the sent data via dmtp is correct
                //if so add new message to the given user address
                //else sends error message to client
                if (request.equals("send")) {
                    String dmtpStatus = dmtpMessage.isValidToSend();
                    if (!dmtpStatus.equals("sendable")) {
                        writer.println(dmtpStatus);
                        writer.flush();
                        continue;
                    }
                    userData.addMessage(dmtpMessage);
                    writer.println("ok");
                    writer.flush();
                    continue;
                }

                //if quit command is received the connection will be closed
                if (request.equals("quit")) {
                    writer.println("ok bye");
                    writer.flush();
                    break;
                }

                response = checkRequest(request);

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
     * if the reqeust is valid, the data will be added to the dmtp object
     *
     * @param request request
     * @return appropriate response to the received request
     */
    private String checkRequest(String request) {
        String[] splitRequest = request.split(" ", 2);

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

    /**
     * Checks if there are any known addresses of the to-field of this dmtp message on this server.
     * @return the addresses of the to-field that are not known on this server.
     */
    private ArrayList<String> userAvailable() {
        ArrayList<String> result = new ArrayList<>();
        for (String userId : dmtpMessage.getTo().split(",")) {
            if (!userData.contains(userId.split("@")[0])) {
                result.add(userId);
            }
        }
        return result;
    }
}

package dslab.mailbox;

import dslab.util.DmtpMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class TransferRequestThread extends Thread {

    private UserData userData;
    private Socket socket;
    private DmtpMessage dmtpMessage = new DmtpMessage();

    public TransferRequestThread(Socket socket,UserData userData) {
        this.socket = socket;
        this.userData = userData;
    }

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
                if(request == null) {
                    break;
                }
                if(request.equals("")) {
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

                if (request.split(" ", 2)[0].equals("to")) {
                    dmtpMessage.setTo(request.split(" ", 2)[1]);
                    ArrayList<String> unknownRecipients = userAvailable();
                    if(!unknownRecipients.isEmpty()){
                        writer.println("error  unknown  recipient" + String.join(" ",unknownRecipients));
                        writer.flush();
                        break;
                    }
                    writer.println( "ok " + request.split(" ", 2)[1].split(",").length);
                    writer.flush();
                }

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
     * if the reqeust is valid, the data will be added to the dmtp object
     * @param request
     * @return
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

    private ArrayList<String> userAvailable(){
        ArrayList<String> result = new ArrayList<>();
        for(String userId : dmtpMessage.getTo().split(",")){
            if(!userData.contains(userId))
            {
                result.add(userId);
            };
        }
        return result;
    }
}

package dslab.transfer;

import dslab.util.Dmtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestThread implements Runnable {

    private ExecutorService messageForwardingExecutorService = Executors.newFixedThreadPool(10);
    private Socket socket;
    private Dmtp dmtp = new Dmtp();

    public RequestThread(Socket socket, ExecutorService messageForwardingExecutorService) {
        this.messageForwardingExecutorService = messageForwardingExecutorService;
        this.socket = socket;
    }


    @Override
    public void run() {
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            writer.println("ok DMTP");
            writer.flush();

            String request;
            boolean beginFlag = false;

            while ((request = reader.readLine()) != null) {
                String response;

                if (!beginFlag && !request.equals("begin")) {
                    writer.println("error wrong start of dmtp");
                    closeSocket();
                }

                if (request.equals("begin")) {
                    beginFlag = true;
                    writer.println("ok");
                    writer.flush();
                    continue;
                }

                if (request.equals("send")) {
                    String dmtpStatus = dmtp.isValidToSend();
                    if (!dmtpStatus.equals("sendable")) {
                        writer.println(dmtpStatus);
                        writer.flush();
                        continue;
                    }
                    messageForwardingExecutorService.submit(new MessageForwardingThread(dmtp));
                }

                if (request.equals("quit")) {
                    writer.println("ok bye");
                    closeSocket();
                }

                response = checkRequest(request);

                // print request
                writer.println(response);
                writer.flush();

                if (response.equals("error  protocol  error")) {
                    closeSocket();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private String checkRequest(String request) {
        String[] splitRequest = request.split(" ", 2);

        if (splitRequest[0].equals("to")) {
            String[] addresses = splitRequest[1].split(",");
            dmtp.setTo(addresses);
            return "ok " + addresses.length;
        }
        if (splitRequest[0].equals("from")) {
            dmtp.setFrom(splitRequest[1]);
            return "ok";
        }
        if (splitRequest[0].equals("subject")) {
            dmtp.setSubject(splitRequest[1]);
            return "ok";
        }
        if (splitRequest[0].equals("data")) {
            dmtp.setData(splitRequest[1]);
            return "ok";
        }
        return "error  protocol  error";
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException ioe) {
            System.out.println("Error closing client connection");
        }
    }
}

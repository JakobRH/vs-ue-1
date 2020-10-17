package dslab.transfer;

import dslab.util.Config;
import dslab.util.Dmtp;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestThread implements Runnable {

    private ExecutorService messageForwardingExecutorService = Executors.newFixedThreadPool(10);
    private Socket socket;
    private Dmtp dmtp = new Dmtp();
    private Config config;

    public RequestThread(Socket socket, ExecutorService messageForwardingExecutorService, Config config) {
        this.messageForwardingExecutorService = messageForwardingExecutorService;
        this.socket = socket;
        this.config = config;
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
                    break;
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
                    messageForwardingExecutorService.submit(new MessageForwardingThread(dmtp, config));
                }

                if (request.equals("quit")) {
                    writer.println("ok bye");
                    writer.flush();
                    break;
                }

                response = checkRequest(request);

                // print request
                writer.println(response);
                writer.flush();

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
}

package dslab.transfer;

import dslab.util.Config;
import dslab.util.DmtpMessage;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * represents thread to handle messageforwarding
 */
public class MessageForwardingThread extends Thread {


    private ArrayList<String> mailboxServers = new ArrayList<>();
    private Config domainConfig = new Config("domains");
    private DmtpMessage dmtpMessage;
    private boolean forwardingSuccess = true; //flag to see if a message could not be sent
    private Config serverConfig;

    /**
     * creates new MessageForwardingThread
     *
     * @param dmtpMessage dmtp info to send to mailbox servers
     * @param config      config to read out needed data for the server
     */
    public MessageForwardingThread(DmtpMessage dmtpMessage, Config config) {
        this.dmtpMessage = dmtpMessage;
        this.serverConfig = config;

        ResourceBundle bundle = ResourceBundle.getBundle("domains");

        mailboxServers.addAll(bundle.keySet());
    }

    /**
     * trie to send dmtp messages to the correct mailboxservers
     * precondition: dmtp object is correct
     */
    @Override
    public void run() {

        for (String mailboxServer : mailboxServers) {
            for (String address : dmtpMessage.getTo().split(",")) {
                if(address.split("@")[1].equals(mailboxServer)){
                    sendMessage(mailboxServer, dmtpMessage);
                    break;
                }

            }

        }

        if (!forwardingSuccess) {
            sendErrorMessageToSender(dmtpMessage.getFrom());
        }

    }

    /**
     * sends information about sender and transferserver to the monitoring server
     *
     * @param sender sender
     */
    private void sendMonitoringStatistics(String sender) {
        DatagramSocket socket = null;
        try {
            InetAddress address = InetAddress.getLocalHost();
            String hostIP = address.getHostAddress();

            socket = new DatagramSocket();

            byte[] buffer;
            DatagramPacket packet;

            String request = hostIP + serverConfig.getInt("tcp.port") + " " + sender;
            buffer = request.getBytes();

            packet = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName(serverConfig.getString("monitoring.host")),
                    serverConfig.getInt("monitoring.port"));
            socket.send(packet);


        } catch (UnknownHostException e) {
            System.out.println("Cannot connect to host: " + e.getMessage());
        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    /**
     * tries to inform the sender, by connecting to the right mailboxserver(if present) and sending the error message
     *
     * @param sender sender
     */
    private void sendErrorMessageToSender(String sender) {

        try {

            for (String mailboxServer : mailboxServers) {

                if (mailboxServer.equals(sender.split("@")[1])) {

                    InetAddress address = InetAddress.getLocalHost();
                    String hostIP = address.getHostAddress();
                    String transferMail = "mailer@" + hostIP;
                    DmtpMessage tmpDmtpMessage = new DmtpMessage(transferMail, sender, "error", "Some messages could not be sent");
                    sendMessage(mailboxServer, tmpDmtpMessage);
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Cannot connect to host: " + e.getMessage());
        }
    }

    /**
     * tries to send message to mailboxserver
     *
     * @param mailboxServer  mailboxserver
     * @param tmpDmtpMessage dmtp to send
     */
    private void sendMessage(String mailboxServer, DmtpMessage tmpDmtpMessage) {
        Socket socket = null;
        String[] mailboxAddress = domainConfig.getString(mailboxServer).split(":");

        try {
            socket = new Socket(mailboxAddress[0], Integer.parseInt(mailboxAddress[1]));


            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream());
            String response;
            boolean serverDmtpAccept = false; //flag to see if server accepts dmtp
            boolean beginSent = false; //flag to see if begin command was already sent
            boolean toSent = false; //flag to see it to command was already sent
            boolean fromSent = false;//flag to see if from command was already sent
            boolean subjectSent = false;//flag to see if subject command was already sent
            boolean dataSent = false;//flag to see if data command was already sent
            boolean dtmpSuccessful = false;//flag to see if dmtp was successfull sent to mailboxserver


            while (!this.isInterrupted()) {
                response = serverReader.readLine();
                if (response == null) {
                    break;
                }
                if (response.equals("")) {
                    continue;
                }

                if ((!response.equals("ok DMTP") && !serverDmtpAccept) || !response.startsWith("ok") || response.equals("ok bye")) {
                    forwardingSuccess = false;
                    break;
                }
                serverDmtpAccept = true;
                if (!beginSent) {
                    serverWriter.println("begin");
                    serverWriter.flush();
                    beginSent = true;
                    continue;
                }
                if (!toSent) {
                    serverWriter.println("to " + tmpDmtpMessage.getTo());
                    serverWriter.flush();
                    toSent = true;
                    continue;
                }
                if (!fromSent) {
                    serverWriter.println("from " + tmpDmtpMessage.getFrom());
                    serverWriter.flush();
                    fromSent = true;
                    continue;
                }
                if (!subjectSent) {
                    serverWriter.println("subject " + tmpDmtpMessage.getSubject());
                    serverWriter.flush();
                    subjectSent = true;
                    continue;
                }
                if (!dataSent) {
                    serverWriter.println("data " + tmpDmtpMessage.getData());
                    serverWriter.flush();
                    dataSent = true;
                    continue;
                }
                if (!dtmpSuccessful) {
                    serverWriter.println("send");
                    serverWriter.flush();
                    dtmpSuccessful = true;
                    continue;
                }
                sendMonitoringStatistics(tmpDmtpMessage.getFrom());
                serverWriter.println("quit");
                serverWriter.flush();
            }

        } catch (UnknownHostException e) {
            System.out.println("Cannot connect to host: " + e.getMessage());
            forwardingSuccess = false;
        } catch (SocketException e) {
            System.out.println("SocketException while handling socket: " + e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

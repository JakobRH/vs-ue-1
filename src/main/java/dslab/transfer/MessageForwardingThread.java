package dslab.transfer;

import dslab.util.Config;
import dslab.util.Dmtp;

import java.io.*;
import java.net.*;

public class MessageForwardingThread implements Runnable{

    private Dmtp dmtp;
    private final static String[] mailboxServers = {"earth.planet", "univer.ze"};
    private final static Config domainConfig = new Config("domains");
    private Config serverConfig;
    public MessageForwardingThread(Dmtp dmtp, Config config) {
        this.dmtp = dmtp;
        this.serverConfig = config;
    }

    @Override
    public void run() {

        for(String toAddress : dmtp.getTo()){
            for(String mailboxServer : mailboxServers){

                if(mailboxServer.equals(toAddress.split("@")[1])){
                    Socket socket = null;
                    String[] mailboxAddress = domainConfig.getString(mailboxServer).split(":");

                    try{
                        socket = new Socket(mailboxAddress[0], Integer.parseInt(mailboxAddress[1]));

                        BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter serverWriter = new PrintWriter(socket.getOutputStream());
                        String response;
                        boolean serverDmtpAccept = false;
                        boolean beginSent = false;
                        boolean toSent = false;
                        boolean fromSent = false;
                        boolean subjectSent = false;
                        boolean dataSent = false;
                        boolean dtmpSuccessful = false;

                        while ((response = serverReader.readLine())!=null) {

                            if((!response.equals("ok DMTP") && !serverDmtpAccept) || !response.startsWith("ok")){
                                break;
                            }
                            if(!beginSent){
                                serverWriter.println("begin");
                                serverWriter.flush();
                                beginSent = true;
                                continue;
                            }
                            if(!toSent){
                                serverWriter.println(toAddress);
                                serverWriter.flush();
                                toSent = true;
                                continue;
                            }
                            if(!fromSent){
                                serverWriter.println(dmtp.getFrom());
                                serverWriter.flush();
                                fromSent = true;
                                continue;
                            }
                            if(!subjectSent){
                                serverWriter.println(dmtp.getSubject());
                                serverWriter.flush();
                                subjectSent = true;
                                continue;
                            }
                            if(!dataSent){
                                serverWriter.println(dmtp);
                                serverWriter.flush();
                                dataSent = true;
                                continue;
                            }
                            if(!dtmpSuccessful){
                                serverWriter.println("send");
                                serverWriter.flush();
                                dtmpSuccessful = true;
                                continue;
                            }
                            sendMonitoringStatistics(dmtp.getFrom());
                            serverWriter.println("quit");
                            serverWriter.flush();
                        }

                    } catch (UnknownHostException e) {
                        System.out.println("Cannot connect to host: " + e.getMessage());
                    } catch (SocketException e) {
                        System.out.println("SocketException while handling socket: " + e.getMessage());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
        }


    }

    private void sendMonitoringStatistics(String sender){
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
}

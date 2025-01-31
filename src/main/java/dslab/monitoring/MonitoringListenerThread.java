package dslab.monitoring;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Represents a MonitoringListenerThread, whichs aim is to accept datagrampackets and to store the data
 * in a thread safe datastructure
 */
public class MonitoringListenerThread extends Thread {

    private DatagramSocket datagramSocket;
    private MonitoringStatistics statistics;

    /**
     * Creates an instance of MonitoringListenerThread
     *
     * @param datagramSocket socket to listen to
     * @param statistics     thread safe data structure to store the sent data
     */
    public MonitoringListenerThread(DatagramSocket datagramSocket, MonitoringStatistics statistics) {
        this.datagramSocket = datagramSocket;
        this.statistics = statistics;
    }

    /**
     * listens to the datagram socket and stores the received data to statistics
     * when this thread gets interrupted the socket will be closed
     */
    public void run() {

        byte[] buffer;
        DatagramPacket packet;

        try {
            while (!this.isInterrupted()) {

                buffer = new byte[1024];
                packet = new DatagramPacket(buffer, buffer.length);

                // wait for incoming packets from client
                datagramSocket.receive(packet);
                // get the data from the packet
                String request = new String(packet.getData(), packet.getOffset(), packet.getLength());
                //add new data to statistics
                statistics.putNewEmailToStatistics(request);

            }

        } catch (SocketException e) {
            // when the socket is closed, the send or receive methods of the DatagramSocket will throw a SocketException
            System.out.println("SocketException while waiting for/handling packets: " + e.getMessage());
        } catch (IOException e) {
            // other exceptions should be handled correctly in your implementation
            throw new UncheckedIOException(e);
        } finally {
            if (datagramSocket != null && !datagramSocket.isClosed()) {
                datagramSocket.close();
            }
        }
    }
}

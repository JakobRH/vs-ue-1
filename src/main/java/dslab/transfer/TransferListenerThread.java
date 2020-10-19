package dslab.transfer;

import dslab.util.Config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferListenerThread extends Thread {

    private ExecutorService requestExecutorService;
    private ExecutorService messageForwardingExecutorService;
    private ServerSocket serverSocket;
    private Config config;

    /**
     * creates new Trasnferlistener
     * @param serverSocket socket to wait for new connection
     * @param config cnofig to pass to new created threads
     */
    public TransferListenerThread(ServerSocket serverSocket, Config config, ExecutorService requestExecutorService, ExecutorService messageForwardingExecutorService) {
        this.serverSocket = serverSocket;
        this.config = config;
        this.requestExecutorService = requestExecutorService;
        this.messageForwardingExecutorService = messageForwardingExecutorService;
    }

    /**
     * waits for new connection on serversocket, then creates new socket and starts a thread to handle the connection
     */
    public void run() {
        try {

            while (!this.isInterrupted()) {
                // wait for Client to connect
                Socket socket = serverSocket.accept();
                requestExecutorService
                        .submit(new RequestThread(socket, this.messageForwardingExecutorService, config));

            }
        } catch (SocketException e) {
            System.out.println("SocketException while waiting for/handling packets: " + e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}

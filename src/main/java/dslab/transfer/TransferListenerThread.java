package dslab.transfer;

import dslab.util.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferListenerThread extends Thread {

    private ExecutorService requestExecutorService = Executors.newFixedThreadPool(10);
    private ExecutorService messageForwardingExecutorService = Executors.newFixedThreadPool(10);
    private ServerSocket serverSocket;
    private Config config;

    public TransferListenerThread(ServerSocket serverSocket, Config config) {
        this.serverSocket = serverSocket;
        this.config = config;
    }

    public void run() {
        while (true) {

            try {
                // wait for Client to connect
              Socket socket = serverSocket.accept();
                requestExecutorService
                        .submit(new RequestThread(socket, this.messageForwardingExecutorService, config));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

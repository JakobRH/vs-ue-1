package dslab.transfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferListenerThread extends Thread {

    private ExecutorService requestExecutorService = Executors.newFixedThreadPool(10);
    private ExecutorService messageForwardingExecutorService = Executors.newFixedThreadPool(10);
    private ServerSocket serverSocket;

    public TransferListenerThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {
        while (true) {

            try {
                // wait for Client to connect
              Socket socket = serverSocket.accept();
                requestExecutorService
                        .submit(new RequestThread(socket, this.messageForwardingExecutorService));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

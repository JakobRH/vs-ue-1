package dslab.mailbox;

import dslab.util.Config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

public class MailboxListenerThreadTransfer extends Thread {

    private ServerSocket serverSocket;
    private Config config;
    private ExecutorService transferExecutorService;
    private UserData userData;

    public MailboxListenerThreadTransfer(ServerSocket serverSocket, Config config, ExecutorService transferExecutorService, UserData userData) {
        this.serverSocket = serverSocket;
        this.config = config;
        this.transferExecutorService = transferExecutorService;
        this.userData = userData;
    }

    public void run() {

        try {

            while (true) {
                // wait for Client to connect
                Socket socket = serverSocket.accept();
                transferExecutorService
                        .submit(new TransferRequestThread(socket, userData));

            }
        } catch (SocketException e) {
            System.out.println("SocketException while waiting for/handling packets: " + e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

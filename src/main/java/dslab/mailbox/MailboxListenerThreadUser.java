package dslab.mailbox;

import dslab.util.Config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

public class MailboxListenerThreadUser extends Thread{

    private ServerSocket serverSocket;
    private Config config;
    private ExecutorService userExecutorService;
    private UserData userData;

    public MailboxListenerThreadUser(ServerSocket serverSocket, Config config, ExecutorService userExecutorService, UserData userData) {
        this.serverSocket = serverSocket;
        this.config = config;
        this.userExecutorService = userExecutorService;
        this.userData = userData;
    }

    public void run() {

        try {

            while (!this.isInterrupted()) {
                // wait for Client to connect
                Socket socket = serverSocket.accept();
                userExecutorService
                        .submit(new UserRequestThread(socket, userData));

            }
        } catch (SocketException e) {
            System.out.println("SocketException while waiting for/handling packets: " + e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}


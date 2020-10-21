package dslab.mailbox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

/**
 * Represents a MailboxListenerThreadUser. Aim of this thread is to handle incoming dmap connections, coming
 * from user.
 */
public class MailboxListenerThreadUser extends Thread {

    private ServerSocket serverSocket;
    private ExecutorService userExecutorService;
    private UserData userData;

    /**
     * Creates new instance of MailboxListenerThreadUser.
     * @param serverSocket the server socket to listen to.
     * @param userExecutorService the executorService to manage new started threads.
     * @param userData the data structure of this server.
     */
    public MailboxListenerThreadUser(ServerSocket serverSocket, ExecutorService userExecutorService, UserData userData) {
        this.serverSocket = serverSocket;
        this.userExecutorService = userExecutorService;
        this.userData = userData;
    }

    /**
     * Waits for new connection on serversocket, then creates new socket and starts a thread to handle the connection
     */
    public void run() {

        try {

            while (!this.isInterrupted()) {
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


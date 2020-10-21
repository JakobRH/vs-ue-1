package dslab.mailbox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

/**
 * Represents a MailboxListenerThreadTransfer. Aim of this thread is to handle incoming dmtp connections, coming
 * from the transfer servers.
 */
public class MailboxListenerThreadTransfer extends Thread {

    private ServerSocket serverSocket;
    private ExecutorService transferExecutorService;
    private UserData userData;

    /**
     * Creates new instance of MailboxListenerThreadTransfer.
     * @param serverSocket the server socket to listen to.
     * @param transferExecutorService the executorService to manage new started threads.
     * @param userData the data structure of this server.
     */
    public MailboxListenerThreadTransfer(ServerSocket serverSocket, ExecutorService transferExecutorService, UserData userData) {
        this.serverSocket = serverSocket;
        this.transferExecutorService = transferExecutorService;
        this.userData = userData;
    }

    /**
     * Waits for new connection on serversocket, then creates new socket and starts a thread to handle the connection.
     */
    public void run() {

        try {

            while (!this.isInterrupted()) {
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

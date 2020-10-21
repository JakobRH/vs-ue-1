package dslab.mailbox;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Represents a MailboxServer.
 */
public class MailboxServer implements IMailboxServer, Runnable {

    private Config config;
    private ServerSocket transferServerSocket;
    private ServerSocket userServerSocket;
    private UserData userData;
    private ExecutorService transferExecutorService = Executors.newFixedThreadPool(10);
    private ExecutorService userExecutorService = Executors.newFixedThreadPool(10);
    private Shell shell;
    private String componentId;
    private MailboxListenerThreadTransfer mailboxListenerThreadTransfer;
    private MailboxListenerThreadUser mailboxListenerThreadUser;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config      the component config
     * @param in          the input stream to read console input from
     * @param out         the output stream to write console output to
     */
    public MailboxServer(String componentId, Config config, InputStream in, PrintStream out) {
        this.config = config;
        this.componentId = componentId;

        getUserData();

        shell = new Shell(in, out);
        shell.register(this);
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }

    /**
     * Creates two server sockets for dmtp and dmap connections and calls for each a new thread to handle
     * incoming connections.
     */
    @Override
    public void run() {
        try {
            transferServerSocket = new ServerSocket(config.getInt("dmtp.tcp.port"));
            mailboxListenerThreadTransfer = new MailboxListenerThreadTransfer(transferServerSocket, transferExecutorService, userData);
            mailboxListenerThreadTransfer.start();

            userServerSocket = new ServerSocket(config.getInt("dmap.tcp.port"));
            mailboxListenerThreadUser = new MailboxListenerThreadUser(userServerSocket, userExecutorService, userData);
            mailboxListenerThreadUser.start();

        } catch (IOException e) {
            throw new UncheckedIOException("Error while creating server socket", e);
        }

        shell.run();
    }

    /**
     * Closes the executorservices and the socket, throws stopshellexecution to shutdown this application
     * interrupts the listenerthread
     * postcondition: all resources used by this server are closed/free
     */
    @Command
    @Override
    public void shutdown() {
        try {
            mailboxListenerThreadTransfer.interrupt();
            mailboxListenerThreadUser.interrupt();
            userExecutorService.shutdownNow();
            transferExecutorService.shutdownNow();
            userServerSocket.close();
            transferServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new StopShellException();
    }

    /**
     * Initializes userData with user already stored in the resources
     */
    private void getUserData() {
        userData = new UserData(new ArrayList<>());
        ResourceBundle bundle = ResourceBundle.getBundle("users-" + this.componentId.split("-", 2)[1]);

        for (String key : bundle.keySet()) {
            userData.addUser(key, bundle.getString(key));
        }
    }
}

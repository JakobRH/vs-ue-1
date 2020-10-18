package dslab.mailbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.transfer.TransferListenerThread;
import dslab.util.Config;

public class MailboxServer implements IMailboxServer, Runnable {

    private Config config;
    private ServerSocket transferServerSocket;
    private ServerSocket userServerSocket;
    private UserData userData;
    private ExecutorService transferExecutorService = Executors.newFixedThreadPool(10);
    private ExecutorService userExecutorService = Executors.newFixedThreadPool(10);
    private Shell shell;
    private String componentId;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MailboxServer(String componentId, Config config, InputStream in, PrintStream out) {
        this.config = config;
        this.componentId = componentId;

        getUserData();

        shell = new Shell(in, out);
        shell.register(this);
    }

    @Override
    public void run() {
        try {
            transferServerSocket = new ServerSocket(config.getInt("dmtp.tcp.port"));
            new MailboxListenerThreadTransfer(transferServerSocket, config, transferExecutorService, userData).start();

            userServerSocket = new ServerSocket(config.getInt("dmap.tcp.port"));
            new MailboxListenerThreadUser(userServerSocket, config, userExecutorService, userData).start();

        } catch (IOException e) {
            throw new UncheckedIOException("Error while creating server socket", e);
        }

        shell.run();
    }

    @Command
    @Override
    public void shutdown() {
        try {
            userExecutorService.shutdownNow();
            transferExecutorService.shutdownNow();
            userServerSocket.close();
            transferServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new StopShellException();
    }

    private void getUserData(){
        userData = new UserData(new ArrayList<>());
        ResourceBundle bundle = ResourceBundle.getBundle("users-" + this.componentId.split("-", 2)[1]);

        for (String key : bundle.keySet()) {
            userData.addUser(key, bundle.getString(key));
        }
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}

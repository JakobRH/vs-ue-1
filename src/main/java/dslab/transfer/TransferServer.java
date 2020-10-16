package dslab.transfer;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.monitoring.MonitoringStatistics;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import dslab.ComponentFactory;
import dslab.util.Config;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferServer implements ITransferServer, Runnable {

    private Config config;
    private ServerSocket serverSocket;
    private Shell shell;
    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public TransferServer(String componentId, Config config, InputStream in, PrintStream out) {
        this.config = config;

        shell = new Shell(in, out);
        shell.register(this);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(config.getInt("tcp.port"));
            // handle incoming connections from client in a separate thread
            new TransferListenerThread(serverSocket).start();
        } catch (IOException e) {
            throw new UncheckedIOException("Error while creating server socket", e);
        }

        shell.run();
    }

    @Command
    @Override
    public void shutdown() {
        // TODO
    }

    public static void main(String[] args) throws Exception {
        ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();
    }

}

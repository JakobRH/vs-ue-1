package dslab.monitoring;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.util.Map;

public class MonitoringServer implements IMonitoringServer {

    private Config config;
    private DatagramSocket datagramSocket;
    private MonitoringStatistics statistics;
    private Shell shell;
    private MonitoringListenerThread monitoringListenerThread;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config      the component config
     * @param in          the input stream to read console input from
     * @param out         the output stream to write console output to
     */
    public MonitoringServer(String componentId, Config config, InputStream in, PrintStream out) {
        this.config = config;
        this.statistics = new MonitoringStatistics();

        shell = new Shell(in, out);
        shell.register(this);
    }

    @Override
    public void run() {

        try {
            datagramSocket = new DatagramSocket(config.getInt("udp.port"));
            monitoringListenerThread = new MonitoringListenerThread(datagramSocket, statistics);
            monitoringListenerThread.start();
        } catch (IOException e) {
            throw new RuntimeException("Cannot listen on UDP port.", e);
        }
        shell.run();
    }

    @Command
    @Override
    public void addresses() {
        Map<String, Integer> addresses = statistics.getAddresses();
        shell.out().flush();

        for (Map.Entry<String, Integer> entry : addresses.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            shell.out().println(key + " " + value);
        }
    }

    @Command
    @Override
    public void servers() {
        Map<String, Integer> servers = statistics.getServers();

        for (Map.Entry<String, Integer> entry : servers.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            shell.out().println(key + " " + value);
        }
    }

    @Command
    @Override
    public void shutdown() {
        monitoringListenerThread.interrupt();
        datagramSocket.close();
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        IMonitoringServer server = ComponentFactory
                .createMonitoringServer(args[0], System.in, System.out);
        server.run();
    }
}

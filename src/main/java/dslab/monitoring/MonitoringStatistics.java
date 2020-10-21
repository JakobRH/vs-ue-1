package dslab.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a thread safe data structure to store the data of sent messages of transfer servers and the according
 * addresses that sent the message
 */
public class MonitoringStatistics {

    private Map<String, Integer> addresses = new ConcurrentHashMap<String, Integer>();
    private Map<String, Integer> servers = new ConcurrentHashMap<String, Integer>();

    /**
     * Creates an instance of MonitoringStatistics
     */
    MonitoringStatistics() {
    }

    /**
     * Updates the statistics.
     *
     * @param emailData data to store
     *                  precondition: emailData should be of the format "<server> <emailAddress>"
     *                  postcondition: data is stored
     */
    public void putNewEmailToStatistics(String emailData) {
        String[] splitEmailData = emailData.split("\\s");
        String server = splitEmailData[0];
        String address = splitEmailData[1];

        synchronized (this) {

            if (servers.containsKey(server)) {
                Integer serverValue = servers.get(server);
                servers.put(server, serverValue + 1);
            } else {
                servers.put(server, 1);
            }
            if (addresses.containsKey(address)) {
                Integer addressValue = addresses.get(address);
                addresses.put(address, addressValue + 1);
            } else {
                addresses.put(address, 1);
            }
        }
    }


    public Map<String, Integer> getAddresses() {
        return addresses;
    }

    public Map<String, Integer> getServers() {
        return servers;
    }
}

package dslab.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitoringStatistics {

    private Map<String, Integer> addresses = new ConcurrentHashMap<String, Integer>();
    private Map<String, Integer> servers = new ConcurrentHashMap<String, Integer>();

    MonitoringStatistics(){

    }

    /**
     * Updates the statistics.
     * @param emailData should be of the format "<server> <emailAddress>"
     */
    public void putNewEmailToStatistics(String emailData){
        String[] splitEmailData = emailData.split("\\s");
        String server = splitEmailData[0];
        String address = splitEmailData[1];

        synchronized (this){

            if(servers.containsKey(server)){
                Integer serverValue = servers.get(server);
                servers.put(server, serverValue+1);
            }
            else{
                servers.put(server, 1);
            }
            if(addresses.containsKey(address)){
                Integer addressValue = addresses.get(address);
                addresses.put(address, addressValue+1);
            }
            else{
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

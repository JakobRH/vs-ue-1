package dslab.mailbox;

import dslab.util.Config;

import java.net.ServerSocket;
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
}


package dslab.mailbox;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class UserRequestThread extends Thread {

    private UserData userData;
    private Socket socket;
    private String userId;


    public UserRequestThread(Socket socket, UserData userData) {
        this.userData = userData;
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            //first response to signal that dmap is accepted
            writer.println("ok DMAP");
            writer.flush();

            String request;
            boolean loggedIn = false;

            while (!this.isInterrupted()) {

                request = reader.readLine();
                if (request == null) {
                    break;
                }
                if (request.equals("")) {
                    continue;
                }

                if (request.startsWith("login")) {
                    if(loggedIn){
                        writer.println("error already logged in");
                        writer.flush();
                        continue;
                    }
                    String userId = request.split(" ")[1];
                    String pw = request.split(" ")[2];
                    String loginResponse = userData.login(userId, pw);
                    writer.println(loginResponse);
                    writer.flush();
                    if (loginResponse.equals("ok")) {
                        loggedIn = true;
                        this.userId = userId;
                    }
                    continue;
                }

                if(request.equals("quit")){
                    writer.println("ok bye");
                    writer.flush();
                    break;
                }

                if (!loggedIn){
                    writer.println("error not logged in");
                    writer.flush();
                    continue;
                }

                if(request.equals("list")){
                    ArrayList<String> list = userData.list(userId);
                    for(String entry : list){
                        writer.println(entry);
                        writer.flush();
                    }
                }

                if(request.startsWith("show")){
                    String messageId = request.split(" ")[1];
                    ArrayList<String> list = userData.show(userId, messageId);
                    for(String entry : list){
                        writer.println(entry);
                        writer.flush();
                    }
                }

                if(request.startsWith("delete")){
                    String messageId = request.split(" ")[1];
                    String response = userData.delete(userId, messageId);
                    writer.println(response);
                    writer.flush();

                }

                if(request.equals("logout")){
                    loggedIn = false;
                    userId = "";
                    writer.println("ok");
                    writer.flush();
                    continue;
                }

            }

        } catch (SocketException e) {
            System.out.println("SocketException while handling socket: " + e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignored because we cannot handle it
                }
            }

        }
    }
}

package dslab.mailbox;

import dslab.util.DmtpMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a user of a mailbox server.
 */
public class MailBoxUser {

    private String userId;
    private String pw;
    private HashMap<Integer, DmtpMessage> messages;
    private int idCounter = 0; //counter to initialize message id

    /**
     * Creates new instance of MailBoxUser.
     * @param userId id of the user
     * @param pw pw of the user
     * @param messages messages of the user
     */
    public MailBoxUser(String userId, String pw, HashMap<Integer, DmtpMessage> messages) {
        this.userId = userId;
        this.messages = messages;
        this.pw = pw;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Adds new message.
     * @param message
     */
    public void addMessage(DmtpMessage message) {
        idCounter = idCounter + 1;
        messages.put(idCounter, message);
    }

    public String getPw() {
        return pw;
    }

    /**
     * Lists all messages of this user in a certain format.
     * @return the list of messages.
     */
    public ArrayList<String> list() {
        ArrayList<String> result = new ArrayList<>();
        for (Integer key : messages.keySet()) {
            result.add(key + " " + messages.get(key).getFrom() + " " + messages.get(key).getSubject());
        }
        return result;
    }

    /**
     * Lists the data of a certain message of this user.
     * @param messageId id of the message
     * @return list of data of this message if it exists, else error message
     */
    public ArrayList<String> show(String messageId) {
        ArrayList<String> result = new ArrayList<>();
        for (Integer key : messages.keySet()) {
            if (messageId.equals(key.toString()))
                result.add(messages.get(key).getFrom());
            result.add(messages.get(key).getTo());
            result.add(messages.get(key).getSubject());
            result.add(messages.get(key).getData());
            return result;
        }
        result.add("error unknown message id");
        return result;
    }

    /**
     * Deletes certain message if it exists.
     * @param messageId id of the message
     * @return string that states the success of the deletion.
     */
    public String delete(String messageId) {
        for (Integer key : messages.keySet()) {
            if (messageId.equals(key.toString()))
                messages.remove(key);
            return "ok";
        }
        return "error unknown message id";
    }

}

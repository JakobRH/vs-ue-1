package dslab.mailbox;

import dslab.util.DmtpMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a thread safe data structure, used to save user and their messages of a mailbox server.
 */
public class UserData {

    private ArrayList<MailBoxUser> user;

    /**
     * Creates new instance of UserData.
     * @param user user
     */
    public UserData(ArrayList<MailBoxUser> user) {
        this.user = user;
    }

    /**
     * Adds new user to the data
     * @param userId id of the user
     * @param pw password of the user account
     *
     * postcondition: new user added to user
     */
    public synchronized void addUser(String userId, String pw) {
        MailBoxUser newUser = new MailBoxUser(userId, pw, new HashMap<>());
        user.add(newUser);
    }

    /**
     * Adds new message to the according user.
     * @param dmtpMessage message data to store
     *
     * precondition: dmptMessage has to be correct
     * postcondition: new message added to user
     */
    public synchronized void addMessage(DmtpMessage dmtpMessage) {

        for (String userId : dmtpMessage.getTo().split(",")) {
            for (MailBoxUser user : user) {
                if (user.getUserId().equals(userId.split("@")[0])) {
                    user.addMessage(dmtpMessage);
                }
            }
        }
    }

    /**
     * Checks if the given userId exists on this user
     * @param userId userId to check
     * @return true if user exists in data, else false
     */
    public boolean contains(String userId) {
        for (MailBoxUser user : user) {
            if (user.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this user exists in the data and if the given password is correct
     * @param userId id of the user that want to login
     * @param pw given password to check if correct
     * @return message that states if the user can login with the given data
     */
    public String login(String userId, String pw) {

        for (MailBoxUser user : user) {
            if (userId.equals(user.getUserId())) {
                if (pw.equals(user.getPw())) {
                    return "ok";
                } else {
                    return "error wrong password";
                }
            }
        }
        return "error unknown user";
    }

    /**
     * Lists all message of a user in a certain format if user exists.
     * @param userId id of the user
     * @return list of message entries
     */
    public ArrayList<String> list(String userId) {
        for (MailBoxUser user : user) {
            if (userId.equals(user.getUserId())) {
                return user.list();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Looks up if a certain message exists.
     * @param userId id of the user
     * @param messageId id of the message
     * @return the data of the message if it exists
     */
    public ArrayList<String> show(String userId, String messageId) {

        for (MailBoxUser user : user) {
            if (userId.equals(user.getUserId())) {
                return user.show(messageId);
            }
        }
        return new ArrayList<>();
    }

    /**
     * Deletes the a certain message if it exists.
     * @param userId id of the user
     * @param messageId if of the message
     * @return message that states if the deletion was successful
     */
    public synchronized String delete(String userId, String messageId) {

        for (MailBoxUser user : user) {
            if (userId.equals(user.getUserId())) {
                return user.delete(messageId);
            }
        }
        return "error unknown user";
    }


}

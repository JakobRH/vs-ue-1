package dslab.mailbox;

import dslab.util.DmtpMessage;

import java.util.ArrayList;

public class MailBoxUser {

    String userId;
    String pw;
    ArrayList<DmtpMessage> messages;

    public MailBoxUser(String userId, String pw, ArrayList<DmtpMessage> messages) {
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


    public void addMessage(DmtpMessage message){
        messages.add(message);
    }
}

package dslab.mailbox;

import dslab.util.DmtpMessage;

import java.util.ArrayList;
import java.util.HashMap;

public class MailBoxUser {

    private String userId;
    private String pw;
    private HashMap<Integer, DmtpMessage> messages;
    private int idCounter = 0;

    public MailBoxUser(String userId, String pw,  HashMap<Integer, DmtpMessage> messages) {
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
        idCounter = idCounter+1;
        messages.put(idCounter, message);
    }

    public String getPw() {
        return pw;
    }

    public ArrayList<String> list(){
        ArrayList<String> result = new ArrayList<>();
        for(Integer key : messages.keySet()){
            result.add(key + " " + messages.get(key).getFrom() + " " + messages.get(key).getSubject());
        }
        return result;
    }

    public ArrayList<String> show(String messageId){
        ArrayList<String> result = new ArrayList<>();
        for(Integer key : messages.keySet()){
            if(messageId.equals(key.toString()))
                result.add(messages.get(key).getFrom());
                result.add(messages.get(key).getTo());
                result.add(messages.get(key).getSubject());
                result.add(messages.get(key).getData());
                return result;
        }
        result.add("error unknown message id");
        return result;
    }

    public String delete(String messageId){
        for(Integer key : messages.keySet()){
            if(messageId.equals(key.toString()))
                messages.remove(key);
            return "ok";
        }
        return "error unknown message id";
    }

}

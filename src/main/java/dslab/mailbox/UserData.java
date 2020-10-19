package dslab.mailbox;

import dslab.util.DmtpMessage;

import java.util.ArrayList;
import java.util.HashMap;

public class UserData {

    private ArrayList<MailBoxUser> user;

    public UserData(ArrayList<MailBoxUser> user) {
        this.user = user;
    }

    public synchronized void addUser(String userId, String pw){
        MailBoxUser newUser = new MailBoxUser(userId, pw, new HashMap<>());
        user.add(newUser);
    }

    public synchronized void addMessage(DmtpMessage dmtpMessage){

        for(String userId : dmtpMessage.getTo().split(",")){
            for(MailBoxUser user : user){
                if(user.getUserId().equals(userId.split("@")[0])){
                    user.addMessage(dmtpMessage);
                }
            }
        }
    }

    public boolean contains(String userId){
        for(MailBoxUser user : user){
            if(user.getUserId().equals(userId)){
                return true;
            }
        }
        return false;
    }

    public String login(String userId, String pw){

        for(MailBoxUser user : user){
            if(userId.equals(user.getUserId())){
                if(pw.equals(user.getPw())){
                    return "ok";
                }
                else{
                    return "error wrong password";
                }
            }
        }
        return "error unknown user";
    }

    public ArrayList<String> list(String userId){
        for(MailBoxUser user : user){
            if(userId.equals(user.getUserId())){
                return user.list();
            }
        }
        return new ArrayList<>();
    }

    public ArrayList<String> show(String userId, String messageId){

        for(MailBoxUser user : user){
            if(userId.equals(user.getUserId())){
                return user.show(messageId);
            }
        }
        return new ArrayList<>();
    }

    public synchronized String delete(String userId, String messageId){

        for(MailBoxUser user : user){
            if(userId.equals(user.getUserId())){
                return user.delete(messageId);
            }
        }
        return "error unknown user";
    }


}

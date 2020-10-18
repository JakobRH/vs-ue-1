package dslab.mailbox;

import dslab.util.DmtpMessage;

import java.util.ArrayList;

public class UserData {

    private ArrayList<MailBoxUser> user;

    public UserData(ArrayList<MailBoxUser> user) {
        this.user = user;
    }

    public synchronized void addUser(String userId, String pw){
        MailBoxUser newUser = new MailBoxUser(userId, pw, new ArrayList<>());
        user.add(newUser);
    }

    public synchronized void addMessage(DmtpMessage dmtpMessage){

    }

    public boolean contains(String userId){
        for(MailBoxUser user : user){
            if(user.userId.equals(userId)){
                return true;
            }
        }
        return false;
    }
}

package dslab.transfer;

import dslab.util.Dmtp;

public class MessageForwardingThread implements Runnable{

    private Dmtp dmtp;

    public MessageForwardingThread(Dmtp dmtp) {
        this.dmtp = dmtp;
    }

    @Override
    public void run() {

    }
}

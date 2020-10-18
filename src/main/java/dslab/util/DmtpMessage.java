package dslab.util;

/**
 * Represents the information a dtmp needs
 */
public class DmtpMessage {

    private String to;
    private String from;
    private String subject;
    private String data;


    public DmtpMessage() {

    }

    /**
     * creates new dmtp object
     * @param from sender
     * @param subject subject
     * @param data data
     */
    public DmtpMessage(String from, String to, String subject, String data) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.data = data;
    }

    /**
     * Checks if the information is enough to make a correct dmtp
     * @return "sendable" if the dtmp object is ready to send, otherwise error message with information on whats missing
     */
    public String isValidToSend() {
        if (to == null) return "error no recipient/s";
        if (from == null) return "error no sender";
        if (subject == null) return "error no subject";
        if (data == null) return "error no data";
        return "sendable";
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}

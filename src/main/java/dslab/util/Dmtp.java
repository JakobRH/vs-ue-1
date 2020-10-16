package dslab.util;

public class Dmtp {

    private String[] to;
    private String from;
    private String subject;
    private String data;


    public Dmtp() {

    }

    public String isValidToSend() {
        if (to == null) return "error no recipient/s";
        if (from == null) return "error no sender";
        if (subject == null) return "error no subject";
        if (data == null) return "error no data";
        return "sendable";
    }

    public String[] getTo() {
        return to;
    }

    public void setTo(String[] to) {
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

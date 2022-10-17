package scc.model;

public class Question {

    public String id;
    public boolean reply; //if this is a reply to the question
    public String auctionId;
    public String userNickname;
    public String message;

    public Question(String id, boolean reply, String auctionId, String userNickname, String message) {
        super();
        this.id = id;
        this.reply = reply;
        this.auctionId = auctionId;
        this.userNickname = userNickname;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getReply() {
        return reply;
    }

    public void setReply(boolean reply) {
        this.reply = reply;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

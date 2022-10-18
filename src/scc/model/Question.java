package scc.model;

public class Question {

    public String id;
    public boolean reply; //if this is a reply to the question
    public String questionId; //if this is a reply to the question then it's the id of the question
    public String auctionId;
    public String userNickname;
    public String message;

    public Question(String id, boolean reply, String questionId, String auctionId, String userNickname, String message) {
        super();
        this.id = id;
        this.reply = reply;
        this.questionId = questionId;
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

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
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

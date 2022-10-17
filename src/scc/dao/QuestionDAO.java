package scc.dao;

public class QuestionsDAO {

    private String _rid;
    private String _ts;
    private String id;
    private boolean reply; //if this is a reply to the question
    private String auctionId;
    private String userNickname;
    private String message;

    public QuestionsDAO() {}; //to deserialize JSON

    public QuestionsDAO(String id, boolean reply, String auctionId, String userNickname, String message) {
        super();
        this.id = id;
        this.reply = reply;
        this.auctionId = auctionId;
        this.userNickname = userNickname;
        this.message = message;
    }

    public String get_rid() {
        return _rid;
    }

    public void set_rid(String _rid) {
        this._rid = _rid;
    }

    public String get_ts() {
        return _ts;
    }

    public void set_ts(String _ts) {
        this._ts = _ts;
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

    @Override
    public String toString() {
        return "QuestionsDAO{_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", reply=" + reply + ", auctionId=" + auctionId + ", userNickname=" + userNickname + ", message=" + message + "}";
    }
}

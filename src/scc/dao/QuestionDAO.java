package scc.dao;

import scc.model.Question;

public class QuestionDAO {

    private String _rid;
    private String _ts;
    private String id;
    private boolean reply; //if this is a reply to the question then it's true
    private String questionId; //if this is a reply to the question then it's the id of the question
    private String auctionId;
    private String userNickname;
    private String message;

    public QuestionDAO() {
    } //to deserialize JSON

    public QuestionDAO(String id, boolean reply, String questionId, String auctionId, String userNickname, String message) {
        super();
        this.id = id;
        this.reply = reply;
        this.questionId = questionId;
        this.auctionId = auctionId;
        this.userNickname = userNickname;
        this.message = message;
    }

    public QuestionDAO(Question question) {
        this(question.getId(), question.isReply(), question.getQuestionId(), question.getAuctionId(), question.getUserNickname(), question.getMessage());
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

    public boolean isReply() {
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

    public Question toQuestion() {
        return new Question(id, reply, questionId, auctionId, userNickname, message);
    }

    @Override
    public String toString() {
        return "QuestionsDAO{_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", reply=" + reply + ", auctionId=" + auctionId + ", userNickname=" + userNickname + ", message=" + message + "}";
    }
}

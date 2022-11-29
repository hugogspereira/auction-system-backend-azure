package scc.dao;

import scc.model.Bid;

public class BidDAO {

    //private String _rid;
    //private String _ts;
    private String _id;
    private String auctionId;
    private String userNickname;
    private float value;

    public BidDAO() {
    }

    public BidDAO(Bid bid) {
        this(bid.getId(), bid.getAuctionId(), bid.getUserNickname(), bid.getValue());
    }

    public BidDAO(String _id, String auctionId, String userNickname, float value) {
        super();
        this._id = _id;
        this.auctionId = auctionId;
        this.userNickname = userNickname;
        this.value = value;
    }

    /*
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
     */

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Bid toBid() {
        return new Bid(_id, auctionId, userNickname, value);
    }

    @Override
    public String toString() {
        return "BidDAO{" +
                "_id='" + _id + '\'' +
                ", auctionId='" + auctionId + '\'' +
                ", userNickname='" + userNickname + '\'' +
                ", value=" + value +
                '}';
    }
}

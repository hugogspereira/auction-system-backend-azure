package scc.model;

public class Bid {

    private String id;
    private String auctionId;
    private String userNickname;
    private float value;

    public Bid(String id, String auctionId, String userNickname, float value) {
        this.id = id;
        this.auctionId = auctionId;
        this.userNickname = userNickname;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}

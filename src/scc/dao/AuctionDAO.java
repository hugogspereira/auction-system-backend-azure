package scc.dao;

import scc.model.Auction;
import scc.model.User;
import scc.utils.AuctionStatus;

import java.util.Arrays;

public class AuctionDAO {

    private String _rid;
    private String _ts;
    private int id;
    private String title;
    private String description;
    private String imageId;
    private String ownerNickname;
    private String endTime;             //type?
    private float minPrice;
    private String winnerNickname;
    private AuctionStatus status;

    public AuctionDAO() {
    }

    public AuctionDAO(Auction a) {
        this(a.getId(), a.getTitle(), a.getDescription(), a.getImageId(), a.getOwnerNickname(), a.getEndTime(), a.getMinPrice(),
        a.getWinnerNickname(), a.getStatus());
    }

    public AuctionDAO(int id, String title, String description, String imageId, String ownerNickname, String endTime,
                      float minPrice, String winnerNickname, AuctionStatus status) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.ownerNickname = ownerNickname;
        this.endTime = endTime;
        this.minPrice = minPrice;
        this.winnerNickname = winnerNickname;
        this.status = status;
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
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getImageId() {
        return imageId;
    }
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
    public String getOwnerNickname() {
        return ownerNickname;
    }
    public void setOwnerNickname(String ownerNickname) {
        this.ownerNickname = ownerNickname;
    }
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public float getMinPrice() {
        return minPrice;
    }
    public void setMinPrice(float minPrice) {
        this.minPrice = minPrice;
    }
    public String getWinnerNickname() {
        return winnerNickname;
    }
    public void setWinnerNickname(String winnerNickname) {
        this.winnerNickname = winnerNickname;
    }
    public AuctionStatus getStatus() {
        return status;
    }
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public Auction toAuction() {
        return new Auction(id, title, description, imageId, ownerNickname, endTime, minPrice, winnerNickname, status);
    }

    @Override
    public String toString() {
        return "AuctionDAO [" +
                "_rid='" + _rid + '\'' +
                ", _ts='" + _ts + '\'' +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageId='" + imageId + '\'' +
                ", ownerNickname='" + ownerNickname + '\'' +
                ", endTime='" + endTime + '\'' +
                ", minPrice=" + minPrice +
                ", winnerNickname='" + winnerNickname + '\'' +
                ", status=" + status +
                ']';
    }

}

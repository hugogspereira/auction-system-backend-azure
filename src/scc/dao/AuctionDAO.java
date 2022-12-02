package scc.dao;

import scc.model.Auction;
import scc.utils.AuctionStatus;

public class AuctionDAO {

    public static String DELETED_USER = "Deleted User";

    //private String _rid;
    //private String _ts;
    private String id;
    private String title;
    private String description;
    private String photoId;
    private String ownerNickname;
    private String endTime;
    private float minPrice;
    private String winnerBid;
    private AuctionStatus status;
    private float winningValue;

    public AuctionDAO() {
    }

    public AuctionDAO(Auction a) {
        this(a.getId(), a.getTitle(), a.getDescription(), a.getPhotoId(), a.getOwnerNickname(), a.getEndTime(), a.getMinPrice());
    }

    public AuctionDAO(String id, String title, String description, String photoId, String ownerNickname,
                      String endTime, float minPrice) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.photoId = photoId;
        this.ownerNickname = ownerNickname;
        this.endTime = endTime;
        this.minPrice = minPrice;
        this.winnerBid = null;
        this.status = AuctionStatus.OPEN;
        this.winningValue = 0;
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

    public String getId() {
        return id;
    }
    public void setId(String id) {
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
    public String getPhotoId() {
        return photoId;
    }
    public void setPhotoId(String imageId) {
        this.photoId = imageId;
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
    public String getWinnerBid() {
        return winnerBid;
    }
    public void setWinnerBid(String winnerBid) {
        this.winnerBid = winnerBid;
    }
    public AuctionStatus getStatus() {
        return status;
    }
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public Auction toAuction() {
        return new Auction(status, id, title, description, photoId, ownerNickname, endTime, minPrice, winnerBid);
    }

    public float getWinningValue() {
        return winningValue;
    }

    public void setWinningValue(float winningValue) {
        this.winningValue = winningValue;
    }

    public boolean isOpen() {
        return status.equals(AuctionStatus.OPEN);
    }

    public boolean isNewValue(float newValue) {
        return newValue >= minPrice && newValue > winningValue;
    }

    @Override
    public String toString() {
        return "AuctionDAO{" +
                "_id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", photoId='" + photoId + '\'' +
                ", ownerNickname='" + ownerNickname + '\'' +
                ", endTime='" + endTime + '\'' +
                ", minPrice=" + minPrice +
                ", winnerBid='" + winnerBid + '\'' +
                ", status='" + status + '\'' +
                ", winningValue='" + winningValue +'\'' +
                '}';
    }
}

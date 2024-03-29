package scc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import scc.utils.AuctionStatus;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Auction {

    private String id;
    private String title;
    private String description;
    private String photoId;
    private String ownerNickname;
    private String endTime;
    private float minPrice;
    private String winnerBid;           //when auctionStatus is OPEN, this is used has the current winner
    private AuctionStatus status;

    public Auction(@JsonProperty("id") String id, @JsonProperty("title") String title, @JsonProperty("description") String description, @JsonProperty("photoId") String photoId,
                   @JsonProperty("ownerNickname") String ownerNickname, @JsonProperty("endTime") String endTime, @JsonProperty("minPrice") float minPrice, @JsonProperty("winnerBid") String winnerBid, @JsonProperty("status") AuctionStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.photoId = photoId;
        this.ownerNickname = ownerNickname;
        this.endTime = ZonedDateTime.parse(endTime).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime().toString();
        this.minPrice = minPrice;
        this.winnerBid = winnerBid;
        this.status = status;
    }
    // Teve de se criar este construtor para quando passamos de AuctionDao para Auction porque se usássemos o já existente iria dar erro pois estaria a fazer duas vezes o parse do ZonedDateTime
    public Auction(AuctionStatus status, String id, String title, String description, String photoId, String ownerNickname, String endTime, float minPrice, String winnerBid) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.photoId = photoId;
        this.ownerNickname = ownerNickname;
        this.endTime = endTime;
        this.minPrice = minPrice;
        this.winnerBid = winnerBid;
        this.status = status;
    }

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
}

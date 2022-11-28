package scc.layers;

import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.QuestionDAO;
import scc.dao.UserDAO;
import java.util.List;

public interface DatabaseLayer {

    // Users
    public void delUserById(String id);

    public UserDAO putUser(UserDAO user);

    public UserDAO getUserById(String id);

    public UserDAO replaceUser(UserDAO user);

    // Auctions
    public AuctionDAO putAuction(AuctionDAO auction);

    public AuctionDAO getAuctionById(String id);

    public AuctionDAO replaceAuction(AuctionDAO auction);

    public List<AuctionDAO> getAuctionsByUser(String nickname);

    public List<AuctionDAO> getAuctionsAboutToClose();

    // Bids
    public BidDAO putBid(BidDAO bid);

    public List<BidDAO> getBidsByAuction(String auctionId);

    public List<BidDAO> getBidsByUser(String nickname);

    public BidDAO replaceBid(BidDAO bid);

    public QuestionDAO putQuestion(QuestionDAO question);

    public QuestionDAO getQuestionById(String id);

    public List<QuestionDAO> getQuestionsByAuctionId(String auctionId);

    public void close();
}

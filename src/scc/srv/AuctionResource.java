package scc.srv;

import com.azure.cosmos.CosmosException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scc.auth.AuthSession;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.QuestionDAO;
import scc.dao.UserDAO;
import scc.layers.BlobPersistentLayer;
import scc.layers.CachenDatabaseLayer;
import scc.layers.CognitiveSearchLayer;
import scc.model.Auction;
import scc.model.Bid;
import scc.model.Question;
import scc.utils.AuctionStatus;
import scc.utils.IdGenerator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Path(AuctionResource.PATH)
public class AuctionResource {

    public static final String PATH = "/auction";

    private final CachenDatabaseLayer cachenDatabaseLayer;
    private final BlobPersistentLayer blobPersistentLayer;
    private final AuthSession auth;
    //private final CognitiveSearchLayer searchLayer;

    public AuctionResource() {
        cachenDatabaseLayer = CachenDatabaseLayer.getInstance();
        blobPersistentLayer = BlobPersistentLayer.getInstance();
        auth = AuthSession.getInstance();
        //searchLayer = CognitiveSearchLayer.getInstance();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Auction createAuction(@CookieParam("scc:session") Cookie session, Auction auction) {
        if(auction == null || auction.getTitle() == null || auction.getDescription() == null || auction.getPhotoId() == null ||
                auction.getOwnerNickname() == null || auction.getEndTime() == null || auction.getMinPrice() <= 0 ||
                cachenDatabaseLayer.getUserById(auction.getOwnerNickname()) == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        auth.checkSession(session, auction.getOwnerNickname());

        if(!blobPersistentLayer.existsBlob(auction.getPhotoId()))
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        if(LocalDateTime.parse(auction.getEndTime()).isBefore(LocalDateTime.now(ZoneId.systemDefault()))) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        auction.setId(IdGenerator.generate());
        return cachenDatabaseLayer.putAuction(auction);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateAuction(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Auction auction) {
        if(id == null || auction == null || auction.getTitle() == null || auction.getDescription() == null || auction.getPhotoId() == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        auth.checkSession(session, auction.getOwnerNickname());

        AuctionDAO auctionDAO = cachenDatabaseLayer.getAuctionById(id);
        if(auctionDAO == null || !blobPersistentLayer.existsBlob(auction.getPhotoId()))
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        if(auctionDAO.isOpen()) {
            auctionDAO.setEndTime(auction.getEndTime());
            if(LocalDateTime.parse(auction.getEndTime()).isBefore(LocalDateTime.now(ZoneId.systemDefault()))) {
                auctionDAO.setStatus(AuctionStatus.CLOSED);
            }
        }
        else {
            if(!LocalDateTime.parse(auction.getEndTime()).equals(LocalDateTime.parse(auctionDAO.getEndTime()))) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
        }

        auctionDAO.setTitle(auction.getTitle());
        auctionDAO.setDescription(auction.getDescription());
        auctionDAO.setPhotoId(auction.getPhotoId());
        cachenDatabaseLayer.replaceAuction(auctionDAO);
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Object> searchAuction(@CookieParam("scc:session") Cookie session, @QueryParam("query") String query) {
        if(query == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        String nickname = auth.getSession(session);
        if(nickname == null)
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);

        auth.checkSession(session, nickname);

        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        //return searchLayer.searchAuctions(query);
    }

    // -------- Bids --------

    @POST
    @Path("/{id}/bid/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Bid createBid(@CookieParam("scc:session") Cookie session, @PathParam("id") String auctionId, Bid bid) {
        if(bid == null || auctionId == null || bid.getUserNickname() == null || bid.getValue() <= 0)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        auth.checkSession(session, bid.getUserNickname());

        AuctionDAO auctionDAO = cachenDatabaseLayer.getAuctionById(auctionId);
        UserDAO userDAO = cachenDatabaseLayer.getUserById(bid.getUserNickname());
        if(auctionDAO == null || userDAO == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        if(!auctionDAO.isOpen() || auctionDAO.getOwnerNickname().equals(bid.getUserNickname()))
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        if(!auctionDAO.isNewValue(bid.getValue()))
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        bid.setId(IdGenerator.generate());
        bid.setAuctionId(auctionId);

        auctionDAO.setWinnerBid(bid.getId());
        auctionDAO.setWinningValue(bid.getValue());
        return cachenDatabaseLayer.putBid(bid,auctionDAO);
    }

    @GET
    @Path("/{id}/bid/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Bid> listBids(@CookieParam("scc:session") Cookie session, @PathParam("id") String auctionId) {
        if(auctionId == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        String uid = auth.getSession(session);
        auth.checkSession(session, uid);

        AuctionDAO auctionDAO = cachenDatabaseLayer.getAuctionById(auctionId);
        if(auctionDAO == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        try {
            List<BidDAO> bidsDAO = cachenDatabaseLayer.getBidsByAuction(auctionId);
            if(bidsDAO == null)
                return null;
            return bidsDAO.stream().map(bidDAO -> bidDAO.toBid()).collect(Collectors.toList());
        } catch(Exception e) {
            throw new WebApplicationException(e);
        }
    }

    // -------- Questions --------

    @POST
    @Path("/{id}/question/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Question createQuestion(@CookieParam("scc:session") Cookie session, @PathParam("id") String auctionId, Question question) {
        if (auctionId == null || question == null || question.getMessage() == null || question.getUserNickname() == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        auth.checkSession(session, question.getUserNickname());

        AuctionDAO auctionDAO = cachenDatabaseLayer.getAuctionById(auctionId);
        if(cachenDatabaseLayer.getUserById(question.getUserNickname()) == null || auctionDAO == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        //in case of a reply, check if the question exists and if the user is the owner of the auction
        if(question.isReply()){
            if(cachenDatabaseLayer.getQuestionById(question.getQuestionId()) == null)
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            if(!auctionDAO.getOwnerNickname().equals(question.getUserNickname()))
                throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        question.setId(IdGenerator.generate());
        question.setAuctionId(auctionId);
        if (question.isReply()) {
            question.setQuestionId(question.getQuestionId());
        } else {
            question.setQuestionId(question.getId());
        }

        try {
            cachenDatabaseLayer.putQuestion(question);
        } catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
        return question;
    }

    @GET
    @Path("/{id}/question/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Question> listQuestions(@CookieParam("scc:session") Cookie session, @PathParam("id") String auctionId) {
        if (auctionId == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        String uid = auth.getSession(session);
        auth.checkSession(session, uid);

        AuctionDAO auctionDAO = cachenDatabaseLayer.getAuctionById(auctionId);
        if(auctionDAO == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        try {
            List<QuestionDAO> list = cachenDatabaseLayer.getQuestionsByAuctionId(auctionId);
            if(list == null)
                return null;
            //removes all replies
            list = list.stream().filter(questionDAO -> !questionDAO.isReply()).collect(Collectors.toList());
            return list.stream().map(QuestionDAO::toQuestion).collect(Collectors.toList());
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Auction> listAuctionsAboutToClose(@CookieParam("scc:session") Cookie session) {

        String uid = auth.getSession(session);
        auth.checkSession(session, uid);

        try {
            List<AuctionDAO> auctionsDAO = cachenDatabaseLayer.getAuctionAboutToClose();
            if (auctionsDAO == null)
                return null;
            return auctionsDAO.stream().map(AuctionDAO::toAuction).collect(Collectors.toList());
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}

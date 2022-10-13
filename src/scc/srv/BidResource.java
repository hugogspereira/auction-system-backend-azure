package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.UserDAO;
import scc.layers.CosmosDBLayer;
import scc.model.Bid;
import scc.utils.IdGenerator;

import javax.ws.rs.Path;
import java.util.List;
import java.util.stream.Collectors;

@Path(AuctionResource.PATH)
public class BidResource {

    private final CosmosDBLayer cosmosDBLayer;

    public BidResource() {
        cosmosDBLayer = CosmosDBLayer.getInstance();
    }

    //TODO: check call exceptions

    @POST
    @Path("/{id}/bid/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Bid createBid(@PathParam("id") String auctionId, Bid bid) {
        if(bid == null || auctionId == null || bid.getUserNickname() == null || bid.getValue() <= 0)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        AuctionDAO auctionDAO;
        UserDAO userDAO;
        try {
            auctionDAO = cosmosDBLayer.getAuctionById(bid.getAuctionId());
            userDAO = cosmosDBLayer.getUserById(bid.getUserNickname());
        } catch(Exception e) {
            throw new WebApplicationException(e);
        }
        if(auctionDAO == null || userDAO == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        bid.setId(IdGenerator.generate());
        bid.setAuctionId(auctionId);

        //TODO

        try {
            cosmosDBLayer.putBid(new BidDAO(bid));
        } catch(Exception e) {
            throw new WebApplicationException(e);
        }
        return bid;
    }


    @GET
    @Path("/{id}/bid/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Bid> listBids(@PathParam("id") String auctionId) {
        if(auctionId == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        AuctionDAO auctionDAO;
        try {
            auctionDAO = cosmosDBLayer.getAuctionById(auctionId);
        } catch(Exception e) {
            throw new WebApplicationException(e);
        }
        if(auctionDAO == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        List<BidDAO> bidsDAO = null;
        try {
            bidsDAO = cosmosDBLayer.getBids();
        } catch(Exception e) {
            throw new WebApplicationException(e);
        }
        if(bidsDAO == null)
            return null;
        return bidsDAO.stream().map(bidDAO -> bidDAO.toBid()).collect(Collectors.toList());
    }

}

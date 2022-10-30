package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scc.dao.AuctionDAO;
import scc.layers.BlobStorageLayer;
import scc.layers.RedisCosmosLayer;
import scc.model.Auction;
import scc.utils.IdGenerator;

@Path(AuctionResource.PATH)
public class AuctionResource {

    //TODO: add auth
    //TODO: end auctions with azure functions

    public static final String PATH = "/auction";

    private final RedisCosmosLayer redisCosmosLayer;
    private final BlobStorageLayer blobStorageLayer;


    public AuctionResource() {
        redisCosmosLayer = RedisCosmosLayer.getInstance();
        blobStorageLayer = BlobStorageLayer.getInstance();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Auction createAuction(Auction auction) {
        if(auction == null || auction.getTitle() == null || auction.getDescription() == null || auction.getPhotoId() == null ||
                auction.getOwnerNickname() == null || auction.getEndTime() == null || auction.getMinPrice() <= 0 ||
                redisCosmosLayer.getUserById(auction.getOwnerNickname()) == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        if(!blobStorageLayer.existsBlob(auction.getPhotoId()))
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        auction.setId(IdGenerator.generate());
        return redisCosmosLayer.putAuction(auction);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateAuction(@PathParam("id") String id, Auction auction) {
        if(id == null || auction == null || auction.getTitle() == null || auction.getDescription() == null || auction.getPhotoId() == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        AuctionDAO auctionDAO = redisCosmosLayer.getAuctionById(id);
        if(auctionDAO == null || !blobStorageLayer.existsBlob(auction.getPhotoId()))
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        auctionDAO.setTitle(auction.getTitle());
        auctionDAO.setDescription(auction.getDescription());
        auctionDAO.setPhotoId(auction.getPhotoId());
        redisCosmosLayer.replaceAuction(auctionDAO);
    }
}

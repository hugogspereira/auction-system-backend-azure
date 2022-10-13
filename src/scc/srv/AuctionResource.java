package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scc.dao.AuctionDAO;
import scc.layers.BlobStorageLayer;
import scc.layers.CosmosDBLayer;
import scc.model.Auction;
import scc.utils.AuctionStatus;
import scc.utils.IdGenerator;

@Path(AuctionResource.PATH)
public class AuctionResource {

    public static final String PATH = "/auction";

    private final CosmosDBLayer cosmosDBLayer;
    private final BlobStorageLayer blobStorageLayer;


    public AuctionResource() {
        cosmosDBLayer = CosmosDBLayer.getInstance();
        blobStorageLayer = BlobStorageLayer.getInstance();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Auction createAuction(Auction auction) {
        if(auction == null || auction.getTitle() == null || auction.getDescription() == null || auction.getPhotoId() == null ||
                auction.getOwnerNickname() == null || auction.getEndTime() == null || auction.getMinPrice() <= 0 ||
                cosmosDBLayer.getUserById(auction.getOwnerNickname()) == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        if(!blobStorageLayer.existsBlob(auction.getPhotoId()))
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        auction.setId(IdGenerator.generate());
        auction.setWinnerNickname(null);
        auction.setStatus(AuctionStatus.OPEN);

        try {
            cosmosDBLayer.putAuction(new AuctionDAO(auction));
        } catch(Exception e) {
            throw new WebApplicationException(e);
        }
        return auction;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateAuction(@PathParam("id") String id, Auction auction) {
        if(id == null || auction == null || auction.getId() == null || auction.getTitle() == null || auction.getDescription() == null ||
                auction.getPhotoId() == null || auction.getOwnerNickname() == null || auction.getEndTime() == null ||
                auction.getMinPrice() <= 0)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        if(cosmosDBLayer.getAuctionById(id) == null || cosmosDBLayer.getUserById(auction.getOwnerNickname()) == null ||
                !blobStorageLayer.existsBlob(auction.getPhotoId()))
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        try {
            cosmosDBLayer.delAuctionById(id);
            cosmosDBLayer.putAuction(new AuctionDAO(auction));
        } catch(Exception e) {
            throw new WebApplicationException(e);
        }
    }
}

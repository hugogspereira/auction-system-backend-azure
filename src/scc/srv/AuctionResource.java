package scc.srv;

import com.azure.cosmos.CosmosException;
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
        } catch(CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
        return auction;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateAuction(@PathParam("id") String id, Auction auction) {
        if(id == null || auction == null || auction.getId() == null || auction.getTitle() == null || auction.getDescription() == null ||
                auction.getPhotoId() == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        AuctionDAO auctionDAO = cosmosDBLayer.getAuctionById(id);
        if(auctionDAO == null || !blobStorageLayer.existsBlob(auction.getPhotoId()))
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        auctionDAO.setTitle(auction.getTitle());
        auctionDAO.setDescription(auction.getDescription());
        auctionDAO.setPhotoId(auction.getPhotoId());
        try {
            cosmosDBLayer.replaceAuction(auctionDAO);
        } catch(CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
    }
}

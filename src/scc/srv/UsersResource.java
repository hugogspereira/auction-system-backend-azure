package scc.srv;

import com.azure.cosmos.CosmosException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import scc.auth.AuthSession;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.UserDAO;
import scc.layers.BlobStorageLayer;
import scc.layers.RedisCosmosLayer;
import scc.model.Auction;
import scc.model.Login;
import scc.model.User;
import scc.utils.Hash;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static scc.dao.AuctionDAO.DELETED_USER;

@Path(UsersResource.PATH)
public class UsersResource {

    public static final String PATH = "/user";

    private final BlobStorageLayer blobStorageLayer;
    private final RedisCosmosLayer redisCosmosLayer;
    private final AuthSession auth;

    public UsersResource() {
        blobStorageLayer = BlobStorageLayer.getInstance();
        redisCosmosLayer = RedisCosmosLayer.getInstance();
        auth = AuthSession.getInstance();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User createUser(User user) {
        if (user == null || user.getNickname() == null || user.getName() == null || user.getPwd() == null || user.getPhotoId() == null || redisCosmosLayer.getUserById(user.getNickname()) != null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        if (!blobStorageLayer.existsBlob(user.getPhotoId())) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        // Create User
        user.setPwd(Hash.of(user.getPwd()));
        return redisCosmosLayer.putUser(user);
    }

    @DELETE
    @Path("/{nickname}")
    @Produces(MediaType.APPLICATION_JSON)
    public User deleteUser(@CookieParam("scc:session") Cookie session, @PathParam("nickname") String nickname, @QueryParam("password") String password) {
        if (nickname == null || password == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        auth.checkSession(session, nickname);

        // Get User
        UserDAO userDao = redisCosmosLayer.getUserById(nickname);
        // See if User exists
        if (userDao == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        checkPwd(userDao.getPwd(), password);

        // Check if there is not an OPEN auction
        List<AuctionDAO> auctions = redisCosmosLayer.getAuctionsByUser(nickname);
        if (auctions.stream().anyMatch(auctionDAO -> auctionDAO.isOpen())) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        //TODO: check if user has a bid on open auctions

        // Delete User
        try {
            auctions.forEach(auctionDAO -> {
                auctionDAO.setOwnerNickname(DELETED_USER);
                redisCosmosLayer.replaceAuction(auctionDAO);
            });
            List<BidDAO> bids = redisCosmosLayer.getBidsByUser(nickname);
            bids.forEach(bidDAO -> {
                bidDAO.setUserNickname(DELETED_USER);
                redisCosmosLayer.replaceBid(bidDAO);
            });
            // TODO: maybe garbage collector
            redisCosmosLayer.deleteUser(nickname);
            //delete the cookie auth
            auth.deleteSession(session);
        } catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
        return userDao.toUser();
    }

    @PUT
    @Path("/{nickname}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateUser(@CookieParam("scc:session") Cookie session, @PathParam("nickname") String nickname, @QueryParam("password") String password, User user) {
        if (nickname == null || password == null || user == null || user.getName() == null || user.getPwd() == null || user.getPhotoId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        auth.checkSession(session, nickname);

        // Get User
        UserDAO userDao = redisCosmosLayer.getUserById(nickname);
        // See if User exists
        if (userDao == null || !blobStorageLayer.existsBlob(user.getPhotoId())) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        checkPwd(userDao.getPwd(), password);
        // Update User
        userDao.setName(user.getName());
        userDao.setPwd(Hash.of(user.getPwd()));
        userDao.setPhotoId(user.getPhotoId());
        redisCosmosLayer.replaceUser(userDao);
    }

    private void checkPwd(String expectedPwd, String pwd) {
        if (!expectedPwd.equals(Hash.of(pwd))) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @GET
    @Path("/{nickname}/auction/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Auction> listUserAuctions(@CookieParam("scc:session") Cookie session, @PathParam("nickname") String nickname) {
        if (nickname == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        auth.checkSession(session, nickname);

        if (redisCosmosLayer.getUserById(nickname) == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        try {
            List<AuctionDAO> auctionsDAO = redisCosmosLayer.getAuctionsByUser(nickname);
            if (auctionsDAO == null)
                return null;
            return auctionsDAO.stream().map(auctionDAO -> auctionDAO.toAuction()).collect(Collectors.toList());
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login user) {

        String id = user.getId();
        String pwd = user.getPwd();

        if (id == null || pwd == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        UserDAO userDAO = redisCosmosLayer.getUserById(id);

        if (userDAO == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (!userDAO.getPwd().equals(Hash.of(pwd))) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        String uid = UUID.randomUUID().toString();
        NewCookie cookie = new NewCookie.Builder("scc:session")
                .value(uid)
                .path("/")
                .comment("sessionid")
                .maxAge(3600)
                .secure(false)
                .httpOnly(true)
                .build();

        auth.putSession(uid, id);
        return Response.ok().cookie(cookie).build();
    }
}

package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.model.User;

@Path("/user")
public class UsersResource {

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createUser(User user) {
        return null;
    }

    @DELETE
    @Path("/{nickname}")
    @Produces(MediaType.APPLICATION_JSON)
    public User deleteUser(@PathParam("nickname") String nickname, @QueryParam("password") String password) {
        return null;
    }


    @PUT
    @Path("/{nickname}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateUser(@PathParam("nickname") String nickname, @QueryParam("password") String password, User user) {

    }
}

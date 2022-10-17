package scc.srv;

import com.azure.cosmos.CosmosException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scc.dao.QuestionDAO;
import scc.layers.CosmosDBLayer;
import scc.model.Question;
import scc.utils.IdGenerator;
import java.util.List;
import java.util.stream.Collectors;

@Path(AuctionResource.PATH)
public class QuestionResource {

    private final CosmosDBLayer cosmosDBLayer;

    public QuestionResource() {
        cosmosDBLayer = CosmosDBLayer.getInstance();
    }

    @POST
    @Path("/{id}/question/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Question createQuestion(@PathParam("id") String auctionId, Question question) {
        if(auctionId == null || question == null || question.getMessage() == null || question.getUserNickname() == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        if(cosmosDBLayer.getUserById(question.getUserNickname()) == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        question.setId(IdGenerator.generate());
        question.setAuctionId(auctionId);

        try {
            cosmosDBLayer.putQuestion(new QuestionDAO(question));
        } catch(CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
        return question;
    }

    @GET
    @Path("/{id}/question/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Question> listQuestions(@PathParam("id") String auctionId) {
        if(auctionId == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        QuestionDAO questionDAO = cosmosDBLayer.getQuestionById(auctionId);

        if(questionDAO == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        try{
            List<QuestionDAO> list = cosmosDBLayer.getQuestionsByAuctionId(auctionId);
            if(list == null)
                return null;
            //removes all replies
            list.removeAll(list.stream().filter(QuestionDAO::getReply).collect(Collectors.toList()));
            return list.stream().map(QuestionDAO::toQuestion).collect(Collectors.toList());
        } catch(Exception e) {
            throw new WebApplicationException(e);
        }
    }
}



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
        if (auctionId == null || question == null || question.getMessage() == null || question.getUserNickname() == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);

        if (cosmosDBLayer.getUserById(question.getUserNickname()) == null || cosmosDBLayer.getAuctionById(auctionId) == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        //in case of a reply, check if the question exists and if the user is the owner of the auction
        if(question.getReply()){
            if(cosmosDBLayer.getQuestionById(question.getQuestionId()) == null)
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            if(!cosmosDBLayer.getAuctionById(auctionId).getOwnerNickname().equals(question.getUserNickname()))
                throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        question.setId(IdGenerator.generate());
        question.setAuctionId(auctionId);
        if (question.getReply()) {
            question.setQuestionId(question.getQuestionId());
        } else {
            question.setQuestionId(question.getId());
        }

        try {
            cosmosDBLayer.putQuestion(new QuestionDAO(question));
        } catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
        return question;
    }

    @GET
    @Path("/{id}/question/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Question> listQuestions(@PathParam("id") String auctionId) {
        if (auctionId == null)
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        QuestionDAO questionDAO = cosmosDBLayer.getQuestionById(auctionId);

        if (questionDAO == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        try {
            List<QuestionDAO> list = cosmosDBLayer.getQuestionsByAuctionId(auctionId);
            if (list == null)
                return null;
            //removes all replies
            list.removeAll(list.stream().filter(QuestionDAO::getReply).collect(Collectors.toList()));
            return list.stream().map(QuestionDAO::toQuestion).collect(Collectors.toList());
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}



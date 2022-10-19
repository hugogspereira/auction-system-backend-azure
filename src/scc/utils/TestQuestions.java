package scc.utils;

import com.azure.cosmos.models.CosmosItemResponse;
import scc.dao.AuctionDAO;
import scc.dao.QuestionDAO;
import scc.dao.UserDAO;
import scc.layers.CosmosDBLayer;

import java.util.List;
import java.util.Locale;

public class TestQuestions {

    public static void main(String[] args) {

        try{
            Locale.setDefault(Locale.US);
            CosmosDBLayer db = CosmosDBLayer.getInstance();
            String id1 = "0:" + System.currentTimeMillis();

            CosmosItemResponse<UserDAO> resUser = null;
            //create a user that is an owner of an auction named "owner"
            UserDAO owner = new UserDAO();
            owner.setNickname(id1);
            owner.setName("owner");
            owner.setPwd("super_secret");
            owner.setPhotoId("0:34253455");
            resUser = db.putUser(owner);

            //create a new user named "client" and add it to the database
            String id2 = "0:" + System.currentTimeMillis();
            UserDAO client = new UserDAO();
            client.setNickname(id2);
            client.setName("client");
            client.setPwd("super_secret");
            client.setPhotoId("0:34253455");
            resUser = db.putUser(client);

            //create a new auction named "auction" and add it to the database
            CosmosItemResponse<AuctionDAO> resAuction = null;
            String id3 = "0:" + System.currentTimeMillis();
            AuctionDAO auction = new AuctionDAO();
            auction.setId(id3);
            auction.setTitle("auction");
            auction.setDescription("this is an auction");
            auction.setPhotoId("0:34253455");
            auction.setOwnerNickname(owner.getNickname());
            auction.setEndTime(Long.toString((System.currentTimeMillis() + 1000000)));
            auction.setMinPrice(10);
            resAuction = db.putAuction(auction);

            //create a new question made by the client about the auction and add it to the database
            CosmosItemResponse<QuestionDAO> resQuestion = null;
            String id4 = "0:" + System.currentTimeMillis();
            QuestionDAO question = new QuestionDAO();
            question.setId(id4);
            question.setQuestionId(id4);
            question.setAuctionId(auction.getId());
            question.setUserNickname(client.getNickname());
            question.setMessage("this is a question");
            question.setReply(false);
            resQuestion = db.putQuestion(question);

            //get the question from the database
            System.out.println("Get for id = " + id4);
            QuestionDAO resGetQuestion = db.getQuestionById(id4);
            System.out.println(resGetQuestion);

            //create a new question that is a reply, made by the owner of the auction, about the previous question and add it to the database
            String id5 = "0:" + System.currentTimeMillis();
            QuestionDAO reply = new QuestionDAO();
            reply.setId(id5);
            reply.setQuestionId(id4);
            reply.setAuctionId(auction.getId());
            reply.setUserNickname(client.getNickname());
            reply.setMessage("this is a reply");
            reply.setReply(true);
            resQuestion = db.putQuestion(reply);

            //create a new question, made by the client, about the auction and add it to the database
            String id6 = "0:" + System.currentTimeMillis();
            QuestionDAO question2 = new QuestionDAO();
            question2.setId(id6);
            question2.setQuestionId(id6);
            question2.setAuctionId(auction.getId());
            question2.setUserNickname(client.getNickname());
            question2.setMessage("this is another question");
            question2.setReply(false);
            resQuestion = db.putQuestion(question2);

            //list all the question made, not replies, about the auction
            List<QuestionDAO> list = db.getQuestionsByAuctionId(auction.getId());
            System.out.println("List of questions: ");
            for(QuestionDAO q : list){
                System.out.println(q);
            }

        }  catch(Exception e) {
            e.printStackTrace();
        }
    }
}

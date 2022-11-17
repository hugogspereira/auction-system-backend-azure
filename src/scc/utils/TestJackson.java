package scc.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.List;

public class TestJackson {

    public static class MyUser {
        public int id;
        public String name;

        //you always need an empty constructor for the mapper to be able to deserialize the object
        public MyUser() {
        }

        public MyUser(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class MyStore {
        public int id;
        public String name;
        public String address;

        public MyStore() {
        }

        public MyStore(int id, String name, String address) {
            this.id = id;
            this.name = name;
            this.address = address;
        }
    }

//    public static void main(String[] args) {
//
//        //create a custom object
//        MyUser user1 = new MyUser(1, "John");
//        MyUser user2 = new MyUser(2, "Jack");
//
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            String json1 = mapper.writeValueAsString(user1);
//            String json2 = mapper.writeValueAsString(user2);
//            System.out.println("user 1 as a json: " + json1);
//
//            //create a list with the objects as jsons
//            List<String> jsons = List.of(json1, json2);
//            System.out.println(mapper.writeValueAsString("this is the list of jsons: " + jsons));
//
//            //convert the list above into a list of MyUser objects
//            List<MyUser> users = mapper.readValue(jsons.toString(), mapper.getTypeFactory().constructCollectionType(List.class, MyUser.class));
//
//
//            System.out.println(mapper.writeValueAsString("deserialized list: " + users));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }


    public static void main (String[] args) {
        MyStore store1 = new MyStore(1, "Store1", "Address1");

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {

            ObjectWriter writer   = mapper.writer().withoutAttribute("property1").withoutAttribute("property2");
            String json1 = writer.writeValueAsString(store1);

            System.out.println("store 1 as a json: " + json1);

            MyUser store = mapper.readValue(json1, MyUser.class);
            System.out.println("deserialized store: " );
            System.out.println(store);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

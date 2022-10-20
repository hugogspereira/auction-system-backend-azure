package scc.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class TestJackson {

    public static class MyUser {
        public int id;
        public String name;

        public MyUser() { }
        public MyUser(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static void main(String[] args) {

        MyUser user1 = new MyUser(1, "John");
        MyUser user2 = new MyUser(2, "Jack");

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json1 = mapper.writeValueAsString(user1);
            String json2 = mapper.writeValueAsString(user2);
            System.out.println(json1);

            List<String> jsons = List.of(json1, json2);

            //convert jsons into a list of MyUser objects
            List<MyUser> users = mapper.readValue(jsons.toString(), mapper.getTypeFactory().constructCollectionType(List.class, MyUser.class));

            System.out.println(mapper.writeValueAsString(jsons));
            System.out.println(mapper.writeValueAsString(users));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

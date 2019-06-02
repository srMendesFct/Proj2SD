package microgram.impl.mongo;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.LinkedList;
import java.util.List;
@Entity("userposts")
public class UserPosts {

    @Id
    String userId;
    List<String> l;

    public UserPosts(String id){
        userId = id;
        l = new LinkedList<String>();

    }

    public void newPost(String userId){
        l.add(userId);
    }
    public List<String> getPosts(){
        return l;
    }

}

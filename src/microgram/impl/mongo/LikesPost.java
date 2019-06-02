package microgram.impl.mongo;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.LinkedList;
import java.util.List;
@Entity("likes")
public class LikesPost {

    @Id
    String postId;
    List<String> l;

    public LikesPost(String id){
        postId = id;
        l = new LinkedList<String>();

    }

    public void newLike(String userId){
        l.add(userId);
    }
    public List<String> getLikes(){
        return l;
    }

}

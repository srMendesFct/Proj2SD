package microgram.impl.mongo;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.LinkedList;
import java.util.List;

@Entity("followers")
public class Followers {
    @Id
    String userId;
    List<String> l;

    public Followers(String id){
        userId = id;
        l = new LinkedList<String>();

    }

    public void newFollower(String userId){
        l.add(userId);
    }
    public List<String> getFollowers(){
        return l;
    }
}

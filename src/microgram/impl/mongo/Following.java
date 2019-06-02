package microgram.impl.mongo;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.LinkedList;
import java.util.List;

@Entity("followings")
public class Following {
    @Id
    String userId;
    List<String> l;

    public Following(String id){
        userId = id;
        l = new LinkedList<String>();

    }

    public void newFollowing(String userId){
        l.add(userId);
    }
    public List<String> getFollwing(){
        return l;
    }
}

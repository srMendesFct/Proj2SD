package microgram.impl.mongo;


import org.mongodb.morphia.annotations.Embedded;

import java.util.List;

@Embedded
public class PojoPostsRelations {

    private List<String> likes;
    private List<String> userposts;

    public PojoPostsRelations(List<String> l, List<String> l2) {
        likes = l;
        userposts = l2;
    }

    public List<String> getlikes() {
        return likes;
    }

    public List<String> getUserposts() {
        return userposts;
    }

    public void setpostsidOfUser(List<String> newl) {
        userposts = newl;
    }

    public void setLikes(List<String> newl) {
        likes = newl;
    }
}

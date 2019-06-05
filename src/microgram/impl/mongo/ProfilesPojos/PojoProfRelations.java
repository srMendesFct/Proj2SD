package microgram.impl.mongo.ProfilesPojos;


import org.mongodb.morphia.annotations.Embedded;

import java.util.LinkedList;
import java.util.List;

@Embedded
public class PojoProfRelations {

    private List<String> followers;
    private List<String> following;
    private List<String> userPostss;

    public PojoProfRelations(List<String> l, List<String> l2) {
        this.following = l;
        this.followers = l2;
        userPostss = new LinkedList<>();
    }

    public void setUserPostss(List<String> userPostss) {
        this.userPostss = userPostss;
    }

    public List<String> getUserPostss() {
        return userPostss;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowers(List<String> newl) {
        followers = newl;
    }

    public void setFollowing(List<String> newl) {
        following = newl;
    }
}

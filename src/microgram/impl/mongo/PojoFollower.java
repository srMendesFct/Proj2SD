package microgram.impl.mongo;

public class PojoFollower {


    String userId;
    String followerId;

    public PojoFollower(String id, String followerId){
        userId = id;
        this.followerId = followerId;

    }

    public String getUserId(){
        return userId;
    }
    public String getFollowers(){
        return followerId;
    }
}

package microgram.impl.mongo;

public class PojoFollowing {
    String userId;
    String followingId;

    public PojoFollowing(String id, String followingId) {
        userId = id;
        this.followingId = followingId;

    }

    public String getUserId() {
        return userId;
    }

    public String getFollwing() {
        return followingId;
    }

}
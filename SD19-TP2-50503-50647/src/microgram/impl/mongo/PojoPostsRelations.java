package microgram.impl.mongo;


import org.mongodb.morphia.annotations.Embedded;

import java.util.List;

@Embedded
public class PojoPostsRelations {
    public final String postId;
    public final String likeduserId;


        public PojoPostsRelations(String postId, String userLiked) {
        this.postId = postId;
        likeduserId = userLiked;
    }

    public String getPostId() {
        return postId;
    }

    public String getUserId() {
        return likeduserId;
    }
}

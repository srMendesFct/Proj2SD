package microgram.impl.mongo;

import microgram.api.Post;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.LinkedList;
import java.util.List;

@Entity("pojoPost")
public class PojoPost {

    @Id
    private final String postId;
    @Id
    private final String userId;

    private Post p;

    @Embedded
    private PojoPostsRelations rel;

    PojoPost(Post p){
        postId = p.getPostId();
        this.p = p;
        rel = new PojoPostsRelations(new LinkedList<>(), new LinkedList<>());
        userId = p.getOwnerId();
    }

    public void setRelLikes(List<String> newl) {
        rel.setLikes(newl);
    }

    public void setRelpostForUser(List<String> newl) {
        rel.setpostsidOfUser(newl);
    }

    public String getPostId() {
        return postId;
    }

    public Post getPost() {
        return p;
    }

    public PojoPostsRelations getRel() {
        return rel;
    }
}

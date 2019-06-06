package microgram.impl.mongo;

import java.util.*;

import com.mongodb.MongoException;
import microgram.api.Post;
import org.apache.zookeeper.server.quorum.Follower;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;

import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Result;


public class MongoPosts implements Posts {
    private static MongoDatabase dbName;
     static MongoCollection<Post> dbCol;
     static MongoCollection<PojoPostsRelations> likescol;


    public MongoPosts() {
        dbCol = dbName.getCollection("posts", Post.class);
        likescol = dbName.getCollection("likes", PojoPostsRelations.class);

        dbCol.createIndex(Indexes.ascending("postId"), new IndexOptions().unique(true));
        likescol.createIndex(Indexes.ascending("postId", "likeduserId"), new IndexOptions().unique(true));
    }


    @Override
    synchronized public Result<Post> getPost(String postId) {
        Post p = dbCol.find(Filters.eq("postId", postId)).first();
        if (p == null) return error(NOT_FOUND);
        else {
            long id1 = likescol.countDocuments(Filters.eq("postId", postId));
            p.setLikes((int) id1);
            return ok(p);
        }
    }

    @Override
    synchronized public Result<String> createPost(Post post) {
        try {
            dbCol.insertOne(post);
            Post currentPost = dbCol.find(Filters.eq("postId", post.getPostId())).first();
            currentPost.setLikes(0);
            return ok(post.getOwnerId());
        } catch (MongoException e) {
            return error(CONFLICT);
        }
    }

    @Override
    synchronized public Result<Void> deletePost(String postId) {
        DeleteResult r = dbCol.deleteOne(Filters.eq("postId", postId));
        if (r.getDeletedCount() == 0) {
            return error(NOT_FOUND);
        } else {
            dbCol.deleteMany(Filters.eq("postId", postId));
            return ok();
        }
    }

    @Override
    synchronized public Result<Void> like(String postId, String userId, boolean isLiked) {

        FindIterable<Post> pf = dbCol.find(Filters.eq("postId", postId));

        if (!(pf.iterator().hasNext())) {
            return error(NOT_FOUND);
        } else {
            PojoPostsRelations rel = likescol.find(Filters.and(Filters.eq("postId", userId), Filters.eq("likeduserId", userId))).first();
            if (isLiked) {

                PojoPostsRelations pfole = new PojoPostsRelations(postId, userId);
                likescol.insertOne(pfole);

            } else {
                likescol.deleteOne(Filters.and(Filters.eq("postId", postId), Filters.eq("likeduserId", userId)));
            }
            return ok();
        }
    }

    @Override
    synchronized public Result<Boolean> isLiked(String postId, String userId) {
        FindIterable<Post> p = dbCol.find(Filters.eq("postId", postId));
        if (!p.iterator().hasNext()) return error(NOT_FOUND);
        else {
            PojoPostsRelations pf = likescol.find(Filters.and(Filters.eq("postId", postId), Filters.eq("likeduserId", userId))).first();
            if (pf != null) return ok(true);
            else return ok(false);
        }
    }

    @Override
    synchronized public Result<List<String>> getPosts(String userId) {
        List<String> l = new LinkedList<>();
        Iterator<Post> fi = dbCol.find().iterator();
        while (fi.hasNext()) {
            Post p = fi.next();
            if (p.getOwnerId().equals(userId)) {
                l.add(p.getPostId());
            }
        }


        if (l.isEmpty()) return error(NOT_FOUND);
        else
            return ok(l);
    }

    @Override
    synchronized public Result<List<String>> getFeed(String userId) {
        List<String> l = new LinkedList<String >();
        Iterator<PojoFollowing> userFollowing = this.getuserFollowing(userId);
        while(userFollowing.hasNext()){
            PojoFollowing pf = userFollowing.next();
            Iterator<Post> postByuser = dbCol.find(Filters.eq("ownerId", pf.getFollwing())).iterator();
            while(postByuser.hasNext())l.add(postByuser.next().getPostId());
        }

        if(l.isEmpty()) return error(NOT_FOUND);
        else{
            return ok(l);
        }
    }

    private MongoCollection<Profile> getProfilesBase() {
        return MongoProfiles.dbCol;
    }

    private Iterator<PojoFollowing>getuserFollowing(String userId){
        return MongoProfiles.followingCol.find(Filters.eq("userId", userId)).iterator();
    }



}

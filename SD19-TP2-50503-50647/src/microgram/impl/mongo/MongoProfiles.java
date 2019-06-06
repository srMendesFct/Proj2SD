package microgram.impl.mongo;

import java.util.*;

import com.mongodb.MongoWriteException;
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
import microgram.api.java.Profiles;
import microgram.api.java.Result;

public class MongoProfiles implements Profiles {


    private static MongoDatabase dbName;
    static MongoCollection<Profile> dbCol;
    private static MongoCollection<PojoFollower> followersCol;
    static MongoCollection<PojoFollowing> followingCol;

    public MongoProfiles(MongoDatabase dbName) {


        dbCol = dbName.getCollection("profiles", Profile.class);
        followersCol = dbName.getCollection("followers", PojoFollower.class);
        followingCol = dbName.getCollection("following", PojoFollowing.class);

        dbCol.createIndex(Indexes.ascending("userId"), new IndexOptions().unique(true));
        followersCol.createIndex(Indexes.ascending("userId", "followerId"), new IndexOptions().unique(true));
        followingCol.createIndex(Indexes.ascending("userId", "followingId"), new IndexOptions().unique(true));


    }

    @Override
    synchronized public Result<Profile> getProfile(String userId) {
        Profile p = dbCol.find(Filters.eq("userId", userId)).first();
        if (p == null) return error(NOT_FOUND);
        else {
            long id1 = followingCol.countDocuments(Filters.eq("userId", userId));
            long id2 = followingCol.countDocuments(Filters.eq("userId", userId));
            p.setFollowing((int) id1);
            p.setFollowers((int) id2);
            return ok(p);
        }
    }

    @Override
    synchronized public Result<Void> createProfile(Profile profile) {
        try {
            dbCol.insertOne(profile);
            Profile currentProfile = dbCol.find(Filters.eq("userId", profile.getUserId())).first();
            currentProfile.setFollowers(0);
            currentProfile.setPosts(0);
            currentProfile.setFollowers(0);
            return ok();
        } catch (MongoWriteException x) {
            return error(CONFLICT);
        }
    }

    @Override
    synchronized public Result<Void> deleteProfile(String userId) {
        DeleteResult r = dbCol.deleteOne(Filters.eq("userId", userId));
        if ((r.getDeletedCount() == 0)) return error(NOT_FOUND);
        else {

            Iterator<PojoFollowing> ipf = followingCol.find(Filters.eq("userId", userId)).iterator();
            while (ipf.hasNext()) {
                PojoFollowing pojo = ipf.next();
                followersCol.deleteMany((Filters.eq("userId", pojo.followingId)));
            }

            Iterator<PojoFollower> helper = followersCol.find(Filters.eq("followerId", userId)).iterator();

            while (helper.hasNext()) {
                PojoFollower pojo = helper.next();
                followingCol.deleteMany((Filters.eq("userId", userId)));
            }

            for (Post p : MongoPosts.dbCol.find(Filters.eq("userId", userId))) {
                MongoPosts.likescol.deleteMany(Filters.eq("postId", p.getPostId()));
                MongoPosts.dbCol.deleteOne(Filters.eq("postId", p.getPostId()));
            }

            return ok();
        }
    }

    @Override
    synchronized public Result<List<Profile>> search(String prefix) {

        List<Profile> listProfiles = new LinkedList<Profile>();
        FindIterable<Profile> found = dbCol.find(Filters.regex("userId", "^" + prefix));
        MongoCursor<Profile> it = found.iterator();
        while (it.hasNext()) {
            listProfiles.add(it.next());
        }
        return ok(listProfiles);

    }

    @Override
    synchronized public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
        FindIterable<PojoFollower> pf = followersCol.find(Filters.eq("userId", userId2));
        FindIterable<PojoFollowing> pfollowin = followingCol.find(Filters.eq("userId", userId1));

        if (!(pf.iterator().hasNext() || pfollowin.iterator().hasNext())) {
            return error(NOT_FOUND);
        } else {
            PojoFollowing folwiSearch = followingCol.find(Filters.and(Filters.eq("userId", userId1), Filters.eq("followingId", userId2))).first();

            if (isFollowing) {
                PojoFollowing pff = new PojoFollowing(userId1, userId2);
                PojoFollower pfole = new PojoFollower(userId2, userId1);
                followingCol.insertOne(pff);
                followersCol.insertOne(pfole);

            } else {
                followingCol.deleteOne(Filters.and(Filters.eq("userId", userId1), Filters.eq("followingId", userId2)));
                followersCol.deleteOne(Filters.and(Filters.eq("userId", userId2), Filters.eq("followerId", userId1)));
            }
            return ok();
        }
    }

    @Override
    synchronized public Result<Boolean> isFollowing(String userId1, String userId2) {
        FindIterable<PojoFollowing> p = followingCol.find(Filters.eq("userId", userId1));
        if (!p.iterator().hasNext()) return error(NOT_FOUND);
        else {
            PojoFollowing pf = followingCol.find(Filters.and(Filters.eq("userId", userId1), Filters.eq("followingId", userId2))).first();
            if (pf != null) return ok(true);
            else return ok(false);
        }
    }

}

package microgram.impl.rest.profiles.replicated;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ErrorCode.NOT_IMPLEMENTED;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.*;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.java.JavaProfiles;
import microgram.impl.mongo.MongoProfiles;
import microgram.impl.rest.replication.MicrogramOperation;
import microgram.impl.rest.replication.MicrogramOperationExecutor;
import microgram.impl.rest.replication.OrderedExecutor;

import java.util.List;

public class ProfilesReplicator implements MicrogramOperationExecutor, Profiles {

	private static final int FOLLOWER = 0, FOLLOWEE = 1;
	
	final Profiles localReplicaDB;
	final OrderedExecutor executor;
	
	ProfilesReplicator( Profiles localDB, OrderedExecutor executor) {
		this.localReplicaDB = localDB;
		this.executor = executor.init(this);
	}
	
	@Override
	public Result<?> execute( MicrogramOperation op ) {
		switch( op.type ) {
			case CreateProfile: {
				return localReplicaDB.createProfile( op.arg( Profile.class));
			}
			case IsFollowing: {
				String[] users = op.args(String[].class);
				return localReplicaDB.isFollowing( users[FOLLOWER], users[FOLLOWEE]);
			}

			case GetProfile: {
				String user = op.args(String.class);
				return localReplicaDB.getProfile(user);
			}
			case DeleteProfile: {
				String user = op.args(String.class);
				return localReplicaDB.deleteProfile(user);
			}

			case SearchProfile: {
				String user = op.args(String.class);
				return localReplicaDB.search(user);
			}

			case FollowProfile: {
				String[] users = op.args(String[].class);
				return localReplicaDB.follow(users[FOLLOWER], users[FOLLOWEE], true);
			}

			case UnFollowProfile: {
				String[] users = op.args(String[].class);
				return localReplicaDB.follow(users[FOLLOWER], users[FOLLOWEE], false);
			}

			default:
				return error(NOT_IMPLEMENTED);
		}	
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		return executor.replicate( new MicrogramOperation(GetProfile, userId));
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		return executor.replicate((new MicrogramOperation(CreateProfile, profile)));

	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		return executor.replicate((new MicrogramOperation(DeleteProfile, userId)));
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return executor.replicate((new MicrogramOperation(SearchProfile,prefix)));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		String[] s = new String[2];
		s[FOLLOWER] = userId1;
		s[FOLLOWEE] = userId2;
		if(isFollowing)
			return executor.replicate((new MicrogramOperation(FollowProfile,s)));

		else
			return executor.replicate((new MicrogramOperation(UnFollowProfile, s)));

	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2)
	{
		String[] s = new String[2];
		s[FOLLOWER] = userId1;
		s[FOLLOWEE] = userId2;
		return executor.replicate((new MicrogramOperation(IsFollowing, s)));
	}


}

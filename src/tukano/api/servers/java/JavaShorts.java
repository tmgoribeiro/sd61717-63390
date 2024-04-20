package tukano.api.servers.java;
import java.util.*;
import java.util.logging.Logger;

import tukano.clients.BlobsClientFactory;
import tukano.clients.UsersClientFactory;
import tukano.Discovery;
import tukano.api.Follows;
import tukano.api.Likes;
import tukano.api.User;
import tukano.api.Short;
import tukano.api.persistence.Hibernate;
import tukano.api.servers.java.Result.ErrorCode;
import tukano.api.servers.rest.RestBlobsServer;


public class JavaShorts implements Shorts {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	Users usersClient = UsersClientFactory.getClient();


	public Result<Short> createShort(String userId, String password){

        Log.info("Try to create short of user: " + userId);

        // Check if user data is valid
        if (userId == null || password == null) {
            Log.info("User object invalid.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

		Result<User> result = usersClient.getUser(userId, password);
		if (result.error() == ErrorCode.NOT_FOUND || result.error() == ErrorCode.FORBIDDEN) {
			return Result.error(result.error());
		}

		String blobURL = BlobsClientFactory.getURI() + "/blobs/" + UUID.randomUUID().toString();

        Short short1 = new Short(UUID.randomUUID().toString(), userId, blobURL);

		Hibernate.getInstance().persist(short1);

        return Result.ok(short1);
    }

	public Result<Void> deleteShort(String shortId, String password){

		Log.info("Short to delete: " + shortId);

		// Check if user data is valid
		if(shortId == null || password == null) {
			return Result.error( Result.ErrorCode.BAD_REQUEST);
		}

		Result<Short> resultShort = getShort(shortId);
		if (resultShort.error() == ErrorCode.NOT_FOUND || resultShort.error() == ErrorCode.FORBIDDEN) {
			return Result.error(resultShort.error());
		}

		Short short1 = resultShort.value();

		Result<User> resultUser = usersClient.getUser(short1.getOwnerId(), password);
		if (resultUser.error() == ErrorCode.NOT_FOUND || resultUser.error() == ErrorCode.FORBIDDEN) {
			return Result.error(resultUser.error());
		}

		List<String> likes = likes(shortId, password).value();
		for (String userId : likes)
			like(shortId, userId, false, password);

		Hibernate.getInstance().delete(short1);

		return Result.ok();
    }

	public Result<Short> getShort(String shortId){

        Log.info(shortId);

        // Check if user data is valid
        if(shortId == null) {
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

        List<Short> shorts = Hibernate.getInstance().jpql(
          "SELECT s FROM Short s WHERE s.shortId = :shortId",
		  Map.of("shortId", shortId),
          Short.class
        );

        if (shorts.isEmpty())    {
            Log.info ("There's no shorts with this shortId");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        Short short1 = shorts.get(0);

        return Result.ok(short1);
    }

	public Result<List<String>> getShorts( String userId ){

        Log.info("Shorts by: " + userId);

        // Check if user data is valid
        if(userId == null) {
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

		if (!doesUserExist(userId)) {
			return Result.error(ErrorCode.FORBIDDEN);
		}

        List<Short> shorts = Hibernate.getInstance().jpql(
          "SELECT s FROM Short s WHERE s.ownerId = :userId",
				Map.of("userId", userId),
				Short.class
        );

		List<String> shortsIds = new ArrayList<>();
		for(Short s : shorts)
			shortsIds.add(s.getShortId());

		return Result.ok(shortsIds);
    }


	private boolean doesUserExist(String userId)	{

		List<User> users = usersClient.searchUsers(userId).value();

		for (User u : users)		{
			if ( u.getUserId().equals(userId))
				return true;
		}
		return false;
	}


	/**
	 * Causes a user to follow the shorts of another user.
	 * 
	 * @param userId1     the user that will follow or cease to follow the
	 *                    followed user
	 * @param userId2     the followed user
	 * @param isFollowing flag that indicates the desired end status of the
	 *                    operation
	 * @param password 	  the password of the follower
	 * @return (OK,), 
	 * 	NOT_FOUND if any of the users does not exist
	 *  FORBIDDEN if the password is incorrect
	 */
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password){

		Log.info("Follow from " + userId1 + " to " + userId2);

		// Check if user data is valid
		if(userId2 == null) {
			return Result.error( Result.ErrorCode.BAD_REQUEST);
		}

		Result<User> result = usersClient.getUser(userId1, password);
		if (result.error() == ErrorCode.NOT_FOUND || result.error() == ErrorCode.FORBIDDEN || result.error() == ErrorCode.BAD_REQUEST) {
			return Result.error(result.error());
		}

		if (!doesUserExist(userId2))    {
			Log.info ("There's no users with this userId");
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		List<Follows> follows = Hibernate.getInstance().jpql(
				"SELECT f FROM Follows f WHERE f.followerId = :userId1 AND f.followedId = :userId2",
				Map.of("userId1", userId1, "userId2", userId2),
				Follows.class
		);

		if (isFollowing)	{
			if (!follows.isEmpty())	{
				return Result.error(Result.ErrorCode.CONFLICT);
			}
			Follows follow = new Follows(userId1, userId2);
			Hibernate.getInstance().persist(follow);
		}
		else {
			if (!follows.isEmpty())	{
				Follows f = follows.get(0);
				Hibernate.getInstance().delete(f);
			}
		}

		/*
		if (!follows.isEmpty() && isFollowing)    {
			Log.info ("User 2 is already followed by user 1");
			return Result.error(Result.ErrorCode.CONFLICT);
		}  else if (!follows.isEmpty() && !isFollowing){
			Follows follow = new Follows(userId1, userId2);
			Hibernate.getInstance().delete(follow);
		} else {
			Follows follow = new Follows(userId1, userId2);
			Hibernate.getInstance().persist(follow);
		}
*/
		return Result.ok();
    }

	public Result<List<String>> followers(String userId, String password){
		Log.info("Followers of: " + userId);

		// Check if user data is valid
		if(userId == null || password == null) {
			return Result.error( Result.ErrorCode.BAD_REQUEST);
		}

		Result<User> result = usersClient.getUser(userId, password);
		if (result.error() == ErrorCode.NOT_FOUND || result.error() == ErrorCode.FORBIDDEN) {
			return Result.error(result.error());
		}

		List<Follows> follows = Hibernate.getInstance().jpql(
				"SELECT f FROM Follows f WHERE f.followedId = :userId",
				Map.of("userId", userId),
				Follows.class
		);

		List<String> followersIds = new ArrayList<>();
		for(Follows f : follows)
			followersIds.add(f.getFollowerId());

		return Result.ok(followersIds);
    }


	/**
	 * Adds or removes a like to a short
	 *
	 * @param shortId  - the identifier of the post
	 * @param userId  - the identifier of the user
	 * @param isLiked - a flag with true to add a like, false to remove the like
	 * @return (OK,void) if the like was added/removed;
	 * 	NOT_FOUND if either the short or the like being removed does not exist,
	 *  CONFLICT if the like already exists.
	 *  FORBIDDEN if the password of the user is incorrect
	 *  BAD_REQUEST, otherwise
	 */
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password){

		Log.info("Like from " + userId + " to short " + shortId);

		// Check if user data is valid
		if(shortId == null || userId == null || password == null) {
			return Result.error( Result.ErrorCode.BAD_REQUEST);
		}

		Result<User> resultUser = usersClient.getUser(userId, password);
		if (resultUser.error() == ErrorCode.NOT_FOUND || resultUser.error() == ErrorCode.FORBIDDEN) {
			return Result.error(resultUser.error());
		}

		Result<Short> resultShort = getShort(shortId);
		if (resultShort.error() == ErrorCode.NOT_FOUND) {
			return Result.error(resultShort.error());
		}

		List<Likes> likes = Hibernate.getInstance().jpql(
				"SELECT l FROM Likes l WHERE l.shortId = :shortId AND l.userId = :userId",
				Map.of("shortId", shortId, "userId", userId),
				Likes.class
		);
		if (!likes.isEmpty() && isLiked)    {
			Log.info ("Short is already liked");
			return Result.error(Result.ErrorCode.CONFLICT);
		} else if (likes.isEmpty() && !isLiked)    {
			Log.info ("Like not found");
			return Result.error(Result.ErrorCode.NOT_FOUND);
		} else if (!likes.isEmpty() && !isLiked) {
			Likes like = new Likes(userId, shortId);
			Short short1 = getShort(shortId).value();
			short1.removeLike();
			Hibernate.getInstance().update(short1);
			Hibernate.getInstance().delete(like);
		} else {
			Likes like = new Likes(userId, shortId);
			Short short1 = getShort(shortId).value();
			short1.addLike();
			Hibernate.getInstance().update(short1);
			Hibernate.getInstance().persist(like);
		}

		return Result.ok();
    }
	

	/**
	 * Returns all the likes of a given short
	 * 
	 * @param shortId the identifier of the short
	 * @param password the password of the owner of the short
	 * @return (OK,List<String>|empty list), 
	 * NOT_FOUND if there is no Short with the given shortId
	 * FORBIDDEN if the password is incorrect
	 */
	public Result<List<String>> likes(String shortId, String password){
		Log.info("Likes of short: " + shortId);

		// Check if user data is valid
		if(shortId == null || password == null) {
			return Result.error( Result.ErrorCode.BAD_REQUEST);
		}

		Result<Short> resultShort = getShort(shortId);
		if (resultShort.error() == ErrorCode.NOT_FOUND) {
			return Result.error(resultShort.error());
		}

		Short short1 = resultShort.value();

		Result<User> resultUser = usersClient.getUser(short1.getOwnerId(), password);
		if (resultUser.error() == ErrorCode.NOT_FOUND || resultUser.error() == ErrorCode.FORBIDDEN) {
			return Result.error(resultUser.error());
		}

		List<Likes> likes = Hibernate.getInstance().jpql(
				"SELECT l FROM Likes l WHERE l.shortId = :shortId",
				Map.of("shortId", shortId),
				Likes.class
		);

		List<String> likesUserIds = new ArrayList<>();
		for(Likes l : likes)
			likesUserIds.add(l.getUserId());

		return Result.ok(likesUserIds);
    }

	public Result<List<String>> getFeed(String userId, String password)	{

		Result<User> result = usersClient.getUser(userId, password);
		if (result.error() == ErrorCode.NOT_FOUND || result.error() == ErrorCode.FORBIDDEN || result.error() == ErrorCode.BAD_REQUEST) {
			return Result.error(result.error());
		}

		List<Short> shortsOnFeed = Hibernate.getInstance().jpql(
				"SELECT s FROM Short s " +
						"    INNER JOIN Follows f ON s.ownerId = f.followedId " +
						"    WHERE f.followerId = :userId " +
						"    UNION " +
						"    SELECT s FROM Short s " +
						"    WHERE s.ownerId = :userId ",
				Map.of("userId", userId),
				Short.class
		);

		Comparator<Short> comparator = Comparator.comparing(Short::getTimestamp).reversed();
		shortsOnFeed.sort(comparator);

		List<String> shortsIds = new ArrayList<>();
		for(Short s : shortsOnFeed)
			shortsIds.add(s.getShortId());

		return	Result.ok(shortsIds);
    }

}

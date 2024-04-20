package tukano.api.servers.java;

import tukano.clients.ShortsClientFactory;
import tukano.api.User;
import tukano.api.persistence.Hibernate;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import tukano.api.servers.java.Result.ErrorCode;


public class JavaUsers implements Users {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());


    @Override
    public Result<String> createUser(User user) {

        Log.info("Try to createUser : " + user.getUserId());

        // Check if user data is valid
        if(user.getUserId() == null || user.getPwd() == null || user.getDisplayName() == null || user.getEmail() == null) {
            Log.info("User object invalid.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

        String userId = user.getUserId();
        List<User> users = Hibernate.getInstance().jpql(
            "SELECT u FROM User u WHERE u.userId = :userId",
            Map.of("userId", userId),
            User.class
        );

        // Insert user, checking if name already exists
        if(!users.isEmpty()) {
            Log.info("User already exists.");
            return Result.error( Result.ErrorCode.CONFLICT);
        }

        Hibernate.getInstance().persist(user);

        return Result.ok( user.getUserId() );
    }

    private boolean isObjectValid(String userId, String pwd) {

        if(userId == null || pwd == null) {
            Log.info("User object invalid.");
            return false;
        }
        return true;
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {

        Log.info(userId + " : " + pwd);

        // Check if user data is valid
        if (!isObjectValid(userId,pwd)) {
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

        List<User> users = Hibernate.getInstance().jpql(
                "SELECT u FROM User u WHERE u.userId = :userId",
                Map.of("userId", userId),
                User.class
        );

        if (users.isEmpty())    {
            Log.info ("There's no users with this userId");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        User user = users.get(0);

        if (!user.getPwd().equals(pwd))    {
            Log.info("Wrong Password");
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }

        return Result.ok(user);
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {

        Log.info("User updated: " + userId);

        // Check if user data is valid
        if(!isObjectValid(userId,pwd) || (user.getUserId() != null && !user.getUserId().equals(userId))) {
            return Result.error( ErrorCode.BAD_REQUEST );
        }

        List<User> users = Hibernate.getInstance().jpql(
                "SELECT u FROM User u WHERE u.userId = :userId",
                Map.of("userId", userId),
                User.class
        );

        if (users.isEmpty())    {
            Log.info ("There's no users with this userId");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        User oldUser = users.get(0);

        if (!oldUser.getPwd().equals(pwd))    {
            Log.info("Wrong Password");
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }


        if(user.getPwd() != null)
            oldUser.setPwd(user.getPwd());
        if(user.getEmail() != null)
            oldUser.setEmail(user.getEmail());
        if(user.getDisplayName() != null)
            oldUser.setDisplayName(user.getDisplayName());

        Hibernate.getInstance().update(oldUser);

        return Result.ok(oldUser);
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {

        Log.info("User to delete: " + userId);



        User user = getUser(userId, pwd).value();

        return Result.ok(user);
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {

        Log.info("Pattern to search: " + pattern);

        if (pattern == null || pattern.trim().isEmpty()) {
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

        String searchPattern = "%" + pattern.toLowerCase() + "%";
        List<User> users;

        if (pattern.trim().isEmpty())   {

            users = Hibernate.getInstance().jpql(
                    "SELECT u FROM User u",
                    Map.of("searchPattern", searchPattern),
                    User.class
            );
        }
        else {
            users = Hibernate.getInstance().jpql(
                    "SELECT u FROM User u WHERE u.userId LIKE :searchPattern",
                    Map.of("searchPattern", searchPattern),
                    User.class
            );
        }

        return Result.ok(users);
    }
}

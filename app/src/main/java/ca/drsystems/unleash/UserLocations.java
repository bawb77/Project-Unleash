package ca.drsystems.unleash;
import java.util.ArrayList;

public class UserLocations {

    static ArrayList<User> userLoc;
    boolean there = false;
    public UserLocations()
    {
        userLoc = new ArrayList<User>();
    }
    public ArrayList<User> returnList()
    {
        return userLoc;
    }
    public void setUser(User user)
    {
        there = false;
        for( User u : userLoc)
        {
            if(user.getNumber() == u.getNumber())
            {
                userLoc.set(user.getNumber(), user);
                there = true;
            }
        }
        if(!there)
        {
            userLoc.add(user);
        }
    }
}
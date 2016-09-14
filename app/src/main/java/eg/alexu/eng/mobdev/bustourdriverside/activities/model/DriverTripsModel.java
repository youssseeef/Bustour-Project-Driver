package eg.alexu.eng.mobdev.bustourdriverside.activities.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import eg.alexu.eng.mobdev.bustourdriverside.activities.utilities.Constants;

/**
 * Created by youss on 8/26/2016.
 */
public class DriverTripsModel {
    //tripId //propertyName, Value
    private static HashMap<String, HashMap<String, String>> tripInfo;
    //tripId //UserIds List
    private static HashMap<String, ArrayList<String>> userListInATrip;

    private static HashMap<String, HashMap<String, Integer>> usersDistance;

    private DriverTripsModel() {
        if (tripInfo == null) {
            tripInfo = new HashMap<>();

        }
        if (userListInATrip == null) {
            userListInATrip = new HashMap<>();
        }
    }


    public static void setUserDistance (String tripId, String userId, Integer distance) {
        if(usersDistance == null)
            usersDistance = new HashMap<>();
        HashMap<String , Integer> temp = usersDistance.get(tripId);
        if(temp == null)
            temp = new HashMap<>();
        temp.put(userId, distance);
        usersDistance.put(tripId, temp);
    }

    public static Integer getUserDistance (String tripId, String userId) {
        if (usersDistance == null)
            return -1;
        return usersDistance.get(tripId) != null ? usersDistance.get(tripId).get(userId) : -1;
    }

    public static void addNewTrip(String tripId, String description, String name) {
        if (tripInfo == null) {
            tripInfo = new HashMap<>();
        }
        HashMap<String, String> hm = new HashMap<>();
        hm.put(Constants.NAME, name);//tripName
        hm.put(Constants.DESCRIPTION, description);
        hm.put(Constants.ENABLE_TRACKING, "false");
        hm.put(Constants.LOC_X, Constants.NO_VALUE);
        hm.put(Constants.LOC_Y, Constants.NO_VALUE);
        hm.put(Constants.ARRIVED, "false");
        tripInfo.put(tripId, hm);
    }


    public static void removeTrip(String tripId) {
        if (tripInfo != null && tripInfo.containsKey(tripId)) {
            tripInfo.remove(tripId);
        }
    }


    public static HashMap<String, String> getTripData(String tripId) {
        if (tripInfo != null && tripInfo.containsKey(tripId)) {
            return tripInfo.get(tripId);
        }
        return null;
    }

    public static List<String> getTripIds() {
        Set<String> s1 = tripInfo.keySet();
        ArrayList<String> al = new ArrayList<>(s1);
        return al;
    }


    //should check before adding a user
    //I have made it like that so the user gets a
    // detailed error message that he already added that person to the trip.
    public static boolean userExistsInATrip(String userId, String tripId) {
        if (userListInATrip != null && userListInATrip.get(tripId) != null) {
            if (userListInATrip.get(tripId).contains(userId)) {
                return true;
            }
        }
        return false;
    }

    public static void addUserInATrip(String tripId, String userId) {
        if (userListInATrip == null) {
            userListInATrip = new HashMap<>();
        }

        if (!userExistsInATrip(userId, tripId) && userListInATrip.get(tripId) != null) {
            ArrayList<String> usersAlreadyInTheTrip = userListInATrip.get(tripId);
            usersAlreadyInTheTrip.add(userId);
            userListInATrip.put(tripId, usersAlreadyInTheTrip);


        } else if (!userExistsInATrip(userId, tripId) && userListInATrip.get(tripId) == null) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(userId);
            userListInATrip.put(tripId, arrayList);
        }

    }

    public static void removeUserInATrip(String tripId, String userId) {
        if (userExistsInATrip(userId, tripId)) {
            int pos = userListInATrip.get(tripId).indexOf(userId);
            userListInATrip.get(tripId).remove(pos);
        }
    }

    public static ArrayList<String> getUsersInATrip(String tripId) {
        if (userListInATrip != null && userListInATrip.get(tripId) != null) {
            return userListInATrip.get(tripId);
        } else return new ArrayList<>();
    }


}

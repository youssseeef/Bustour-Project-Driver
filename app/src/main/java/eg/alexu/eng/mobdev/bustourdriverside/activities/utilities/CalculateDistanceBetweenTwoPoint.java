package eg.alexu.eng.mobdev.bustourdriverside.activities.utilities;

public abstract class CalculateDistanceBetweenTwoPoint {

    public final static double RING_DISTANCE = 50.0;

    public static double distanceBetweenTwoCoordinates (double lat1, double lat2, double lon1,
                             double lon2) {

        final int radiusOfTheEarth = 6371;

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = radiusOfTheEarth * c * 1000;

        return distance;
    }

    public static boolean checkTargetReached (double distance) {
        return distance <= RING_DISTANCE;
    }


}

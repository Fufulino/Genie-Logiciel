package com.charitymap.gui;

import com.charitymap.model.Point;

/**
 * GPS projection calculations.
 */
public class GeoProjection {

    /**
     * Minimum latitude bound for the Cergy map.
     */
    public static final double LAT_MIN = 48.9950;

    /**
     * Maximum latitude bound for the Cergy map.
     */
    public static final double LAT_MAX = 49.0700;

    /**
     * Minimum longitude bound for the Cergy map.
     */
    public static final double LON_MIN = 1.9500;

    /**
     * Maximum longitude bound for the Cergy map.
     */
    public static final double LON_MAX = 2.0950;

    /**
     * Width of the map image in nominal pixels.
     */
    public static final double NOMINAL_WIDTH = 1000.0;

    /**
     * Height of the map image in nominal pixels.
     */
    public static final double NOMINAL_HEIGHT = 1000.0;

    /**
     * Ratio converting pixels to real-world meters.
     */
    public static final double PIXEL_TO_METERS = 5.7;

    /**
     * Private constructor to prevent instantiation.
     */
    private GeoProjection() {}

    /**
     * Projects world/pixel coordinates back to GPS latitude/longitude.
     *
     * @param x the world x coordinate
     * @param y the world y coordinate
     * @return double array where index 0 is latitude and index 1 is longitude
     */
    public static double[] toGps(double x, double y) {
        double lon = LON_MIN + (x / NOMINAL_WIDTH) * (LON_MAX - LON_MIN);
        double lat = LAT_MAX - (y / NOMINAL_HEIGHT) * (LAT_MAX - LAT_MIN);
        return new double[] { lat, lon };
    }

    /**
     * Projects GPS latitude/longitude coordinates to world/pixel coordinates.
     *
     * @param lat the latitude
     * @param lon the longitude
     * @return the projected Point in pixels
     */
    public static Point toPixel(double lat, double lon) {
        double x = ((lon - LON_MIN) / (LON_MAX - LON_MIN)) * NOMINAL_WIDTH;
        double y = ((LAT_MAX - lat) / (LAT_MAX - LAT_MIN)) * NOMINAL_HEIGHT;
        return new Point(x, y);
    }

    /**
     * Formats the coordinates of a pixel point as a GPS string.
     *
     * @param x the world x coordinate
     * @param y the world y coordinate
     * @return formatted GPS coordinate string
     */
    public static String formatGps(double x, double y) {
        double[] gps = toGps(x, y);
        return String.format("%.5f° N, %.5f° E", gps[0], gps[1]);
    }

    /**
     * Formats a distance in pixels to a human-readable meters/kilometers string.
     *
     * @param pixelDistance the distance in pixels
     * @return formatted distance string
     */
    public static String formatDistance(double pixelDistance) {
        double meters = pixelDistance * PIXEL_TO_METERS;
        if (meters >= 1000.0) {
            return String.format("%.2f km", meters / 1000.0);
        } else {
            return String.format("%.0f m", meters);
        }
    }
}

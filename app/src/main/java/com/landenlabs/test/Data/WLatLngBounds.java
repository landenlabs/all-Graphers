package com.landenlabs.test.Data;


import android.annotation.SuppressLint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Wrapper class to extend behavior of TWCMapBounds
 *
 * @see WLatLng
 */

@SuppressWarnings("WeakerAccess")
public final class WLatLngBounds {
    public  WLatLng northeast;
    public  WLatLng southwest;
    // public static final LatLngBoundsCreator CREATOR = new LatLngBoundsCreator();
    // private final int version;

    @SuppressLint("RestrictedApi")
    public WLatLngBounds(WLatLng northeast, WLatLng southwest) {
        // this.version = version;
        this.southwest = southwest;
        this.northeast = northeast;
        // WxPreconditions.checkState(southwest.latitude <= northeast.latitude, "Latitudes out of order");
        // Can span dateline
        // WxPreconditions.checkState(southwest.longitude <= northeast.longitude, "Longitdes out of order");
    }

    public WLatLngBounds(ArrayList<WLatLng> points) {
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = Double.MIN_VALUE;
        for (WLatLng point : points) {
            minLat = Math.min(minLat, point.latitude);
            maxLat = Math.max(maxLat, point.latitude);
            minLng = Math.min(minLng, point.longitude);
            maxLng = Math.max(maxLng, point.longitude);
        }
        this.southwest = new WLatLng(minLat, minLng);
        this.northeast = new WLatLng(maxLat, maxLng);
    }

    @SuppressLint("RestrictedApi")
    public WLatLngBounds(WLatLng center, double radiusKm) {
        // this.version = version;
        double deltaLat = earthTravel(center, radiusKm*1000, 0).latitude - center.latitude;  // north;
        double deltaLng = earthTravel(center, radiusKm*1000, 90).longitude - center.longitude;  // east;

        this.southwest = new WLatLng(center.latitude - deltaLat, center.longitude - deltaLng);
        this.northeast = new WLatLng(center.latitude + deltaLat, center.longitude +deltaLng);

        // WxPreconditions.checkState(southwest.latitude <= northeast.latitude, "Latitudes out of order");
        // Can span dateline
        // WxPreconditions.checkState(southwest.longitude <= northeast.longitude, "longitude out of order");
    }

    private static final WLatLngBounds EMPTY = new WLatLngBounds(WLatLng.empty(), WLatLng.empty());
    public static WLatLngBounds empty() {
        return EMPTY;
    }

    public void add(WLatLng point) {
        this.southwest = new WLatLng(
                Math.min(southwest.latitude, point.latitude),
                Math.min(southwest.longitude, point.longitude));
        this.northeast = new WLatLng(
                Math.max(northeast.latitude, point.latitude),
                Math.max(northeast.longitude, point.longitude));
    }

    public void add(WLatLngBounds other) {
        this.southwest = new WLatLng(
                Math.min(southwest.latitude, other.southwest.latitude),
                Math.min(southwest.longitude, other.southwest.longitude));
        this.northeast = new WLatLng(
                Math.max(northeast.latitude, other.northeast.latitude),
                Math.max(northeast.longitude, other.northeast.longitude));
    }


    public WLatLng southeast() {
        return new WLatLng(southwest.latitude, northeast.longitude);
    }
    public WLatLng northwest() {
        return new WLatLng(northeast.latitude, southwest.longitude);
    }


    /**
     * Returns the LatLng resulting from moving a distance from an origin
     * in the specified heading (expressed in degrees clockwise from north).
     * @param fromDeg     The LatLng from which to start.
     * @param distanceMeters The distance to travel.
     * @param headingDegrees  The heading in degrees clockwise from north.
     */
    public static WLatLng earthTravel(WLatLng fromDeg, double distanceMeters, double headingDegrees) {
        final double EARTH_RADIUS_METERS = 6378137;
        distanceMeters /=  EARTH_RADIUS_METERS;
        headingDegrees = Math.toRadians(headingDegrees);
        // http://williams.best.vwh.net/avform.htm#LL
        double fromLat = Math.toRadians(fromDeg.latitude);
        double fromLng = Math.toRadians(fromDeg.longitude);
        double cosDistance = Math.cos(distanceMeters);
        double sinDistance = Math.sin(distanceMeters);
        double sinFromLat = Math.sin(fromLat);
        double cosFromLat = Math.cos(fromLat);
        double sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * Math.cos(headingDegrees);
        double dLng = Math.atan2(
                sinDistance * cosFromLat * Math.sin(headingDegrees),
                cosDistance - sinFromLat * sinLat);
        return new WLatLng(Math.toDegrees(Math.asin(sinLat)), Math.toDegrees(fromLng + dLng));
    }


    /* MAP2 not sure we need these
    public WLatLngBounds(@NonNull  com.weather.pangea.geom.LatLng northEast, @NonNull  com.weather.pangea.geom.LatLng southWest) {
        this(new WLatLng(northEast), new WLatLng(southWest));
    }

    public WLatLngBounds(@NonNull  com.mapbox.mapboxsdk.geometry.LatLng northEast, @NonNull  com.mapbox.mapboxsdk.geometry.LatLng southWest) {
        this(new WLatLng(northEast), new WLatLng(southWest));
    }

    public WLatLngBounds(@NonNull  com.mapbox.mapboxsdk.geometry.LatLngBounds mapboxBnds) {
        this(mapboxBnds.getNorthEast(), mapboxBnds.getSouthWest());
    }

    @SuppressWarnings("unused")
    public com.mapbox.mapboxsdk.geometry.LatLngBounds toMapLL() {
        return com.mapbox.mapboxsdk.geometry.LatLngBounds.from(northeast.latitude, northeast.longitude, southwest.latitude, southwest.longitude);
    }

    @SuppressWarnings("unused")
    public com.weather.pangea.geom.LatLngBounds toPangeaLL() {
        return new com.weather.pangea.geom.LatLngBounds(northeast.toPangeaLL(), southwest.toPangeaLL());
    }
    */

    public static Builder builder() {
        return new Builder();
    }

    public boolean containsLat(double lat) {
        return (this.southwest.latitude <= lat)
                && (lat <= this.northeast.latitude);
    }

    public boolean containsLng(double lng) {
        if (this.southwest.longitude <= this.northeast.longitude) {
            return (this.southwest.longitude <= lng)
                    && (lng <= this.northeast.longitude);
        }
        return (this.southwest.longitude <= lng)
                || (lng <= this.northeast.longitude);
    }

    public boolean contains(WLatLng paramLatLng) {
        return containsLat(paramLatLng.latitude) && containsLng(paramLatLng.longitude);
    }

    public boolean isOverlapping(WLatLngBounds bounds) {
        // bounds below or above of us
        if (bounds.northeast.latitude < southwest.latitude || bounds.southwest.latitude > northeast.latitude)
            return false;
        // bounds left or right of us
        if (bounds.northeast.longitude < southwest.longitude || bounds.southwest.longitude > northeast.longitude)
            return false;
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WLatLngBounds)) {
            return false;
        }
        WLatLngBounds localLatLngBounds = (WLatLngBounds) other;
        return (this.southwest.equals(localLatLngBounds.southwest))
                && (this.northeast.equals(localLatLngBounds.northeast));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.southwest, this.northeast);
    }

    @Override
    @NonNull
    public String toString() {
        return "southwest " + this.southwest + " northeast " + this.northeast;
    }


    /**
     * Return center of smallest bounding box
     */
    public WLatLng getCenter() {
        double centerLat = (this.southwest.latitude + this.northeast.latitude) / 2.0D;
        double right = this.northeast.longitude;
        double left = this.southwest.longitude;
        double lngSum = left + right;
        if (left * right < 0 && Math.abs(left - right) > 180) {
            lngSum += (lngSum > 0) ? -360 : 360;
        }
        double centerLng = lngSum / 2.0D;

        return new WLatLng(centerLat, centerLng);
    }

    @SuppressWarnings("unused")
    public RectF getBoundsRegion() {
        float left = (float)this.southwest.longitude;
        float right = (float)this.northeast.longitude;
        if (left > right) {
            if (Math.abs(left) < Math.abs(right)) {
                left -= 360.0F;
            } else {
                right += 360.0F;
            }
        }

        return new RectF(left, (float)this.northeast.latitude, right, (float)this.southwest.latitude);
    }

    /**
     * Get the  distance, in degrees, between the north and
     * south boundaries.
     */
    @SuppressWarnings("unused")
    public double getLatitudeSpan() {
        return (this.northeast.latitude - this.southwest.latitude);
    }

    /**
     * Get the  distance, in degrees, between the west and east boundaries.
     */
    public double getLongitudeSpan() {
        double longSpanDeg = this.northeast.longitude - this.southwest.longitude;
        return (longSpanDeg >= 0) ? longSpanDeg : longSpanDeg + 360;
    }


    public static final class Builder {
        private double bottom = Double.NaN;     //  (1.0D / 0.0D);
        private double top = Double.NaN;        // (-1.0D / 0.0D);
        private double left = Double.NaN;       // (0.0D / 0.0D);
        private double right = Double.NaN;      // (0.0D / 0.0D);

        public WLatLngBounds build() {
            return new WLatLngBounds(
                    new WLatLng(this.top, this.right),      // NorthEast
                    new WLatLng(this.bottom, this.left));   // SouthWest
        }

        public Builder include(WLatLng paramLatLng) {
            if (Double.isNaN(this.bottom)) {
                this.bottom = this.top = paramLatLng.latitude;
            } else {
                this.bottom = Math.min(this.bottom, paramLatLng.latitude);
                this.top = Math.max(this.top, paramLatLng.latitude);
            }
            double lng = WLatLng.normalizeLng(paramLatLng.longitude);
            if (Double.isNaN(this.left)) {
                this.right = this.left = lng;
            } else {
                this.left = Math.min(this.left, lng);
                this.right = Math.max(this.right, lng);
            }
            return this;
        }
    }
}

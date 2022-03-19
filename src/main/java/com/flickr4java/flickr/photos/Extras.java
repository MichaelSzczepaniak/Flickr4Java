
package com.flickr4java.flickr.photos;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Extra-attributes for Photo-requests.
 * 
 * @author Anthony Eden
 * @version $Id: Extras.java,v 1.12 2009/07/23 20:41:03 x-mago Exp $
 */
public class Extras {

    public static final String KEY_EXTRAS = "extras";

    public static final String LICENSE = "license";

    public static final String DATE_UPLOAD = "date_upload";

    public static final String DATE_TAKEN = "date_taken";

    public static final String OWNER_NAME = "owner_name";

    public static final String ICON_SERVER = "icon_server";

    public static final String ORIGINAL_FORMAT = "original_format";

    public static final String LAST_UPDATE = "last_update";

    public static final String GEO = "geo";

    public static final String TAGS = "tags";

    public static final String MACHINE_TAGS = "machine_tags";

    public static final String O_DIMS = "o_dims";

    public static final String MEDIA = "media";

    public static final String VIEWS = "views";

    public static final String PATH_ALIAS = "path_alias";

    public static final String URL_S = "url_s";

    public static final String URL_SQ = "url_sq";

    public static final String URL_T = "url_t";

    public static final String URL_M = "url_m";

    public static final String URL_L = "url_l";

    public static final String URL_O = "url_o";

    /**
     * Set of all extra-arguments. Used for requesting lists of photos.
     * 
     * @see com.flickr4java.flickr.groups.pools.PoolsInterface#getPhotos(String, String[], Set, int, int)
     * @see com.flickr4java.flickr.panda.PandaInterface#getPhotos(com.flickr4java.flickr.panda.Panda, Set, int, int)
     * @see com.flickr4java.flickr.people.PeopleInterface#getPublicPhotos(String, Set, int, int)
     * @see com.flickr4java.flickr.photosets.PhotosetsInterface#getPhotos(String, Set, int, int, int)
     * @see com.flickr4java.flickr.photos.PhotosInterface#getContactsPublicPhotos(String, Set, int, boolean, boolean, boolean)
     * @see com.flickr4java.flickr.photos.PhotosInterface#getWithGeoData(java.util.Date, java.util.Date, java.util.Date, java.util.Date, int, String, Set, int,
     *      int)
     * @see com.flickr4java.flickr.photos.PhotosInterface#getWithoutGeoData(java.util.Date, java.util.Date, java.util.Date, java.util.Date, int, String, Set,
     *      int, int)
     * @see com.flickr4java.flickr.photos.PhotosInterface#recentlyUpdated(java.util.Date, Set, int, int)
     * @see com.flickr4java.flickr.photos.SearchParameters#setExtras(Set)
     * @see com.flickr4java.flickr.photos.geo.GeoInterface#photosForLocation(GeoData, Set, int, int)
     * @see com.flickr4java.flickr.interestingness.InterestingnessInterface#getList(java.util.Date, Set, int, int)
     * @see com.flickr4java.flickr.favorites.FavoritesInterface#getList(String, int, int, Set)
     */
    //public static final Set<String> ALL_EXTRAS = new HashSet<String>();     // Issue 2
    protected static final Set<String> ALL_EXTRAS_INIT = new HashSet<>();  // Issue 2, FIX

    /**
     * Minimal Set of extra-arguments. Used by convenience-methods that request lists of photos.
     * 
     * @see com.flickr4java.flickr.groups.pools.PoolsInterface#getPhotos(String, String[], Set, int, int)
     * @see com.flickr4java.flickr.panda.PandaInterface#getPhotos(com.flickr4java.flickr.panda.Panda, Set, int, int)
     * @see com.flickr4java.flickr.people.PeopleInterface#getPublicPhotos(String, Set, int, int)
     * @see com.flickr4java.flickr.photosets.PhotosetsInterface#getPhotos(String, Set, int, int, int)
     * @see com.flickr4java.flickr.photos.PhotosInterface#getContactsPublicPhotos(String, Set, int, boolean, boolean, boolean)
     * @see com.flickr4java.flickr.photos.PhotosInterface#getWithGeoData(java.util.Date, java.util.Date, java.util.Date, java.util.Date, int, String, Set, int,
     *      int)
     * @see com.flickr4java.flickr.photos.PhotosInterface#getWithoutGeoData(java.util.Date, java.util.Date, java.util.Date, java.util.Date, int, String, Set,
     *      int, int)
     * @see com.flickr4java.flickr.photos.PhotosInterface#recentlyUpdated(java.util.Date, Set, int, int)
     * @see com.flickr4java.flickr.photos.geo.GeoInterface#photosForLocation(GeoData, Set, int, int)
     * @see com.flickr4java.flickr.interestingness.InterestingnessInterface#getList(java.util.Date, Set, int, int)
     * @see com.flickr4java.flickr.favorites.FavoritesInterface#getList(String, int, int, Set)
     */
    protected static final Set<String> MIN_EXTRAS_INIT = new HashSet<>();

    static {
        ALL_EXTRAS_INIT.add(DATE_TAKEN);
        ALL_EXTRAS_INIT.add(DATE_UPLOAD);
        ALL_EXTRAS_INIT.add(ICON_SERVER);
        ALL_EXTRAS_INIT.add(LAST_UPDATE);
        ALL_EXTRAS_INIT.add(LICENSE);
        ALL_EXTRAS_INIT.add(ORIGINAL_FORMAT);
        ALL_EXTRAS_INIT.add(OWNER_NAME);
        ALL_EXTRAS_INIT.add(GEO);
        ALL_EXTRAS_INIT.add(TAGS);
        ALL_EXTRAS_INIT.add(MACHINE_TAGS);
        ALL_EXTRAS_INIT.add(O_DIMS);
        ALL_EXTRAS_INIT.add(MEDIA);
        ALL_EXTRAS_INIT.add(VIEWS);
        ALL_EXTRAS_INIT.add(PATH_ALIAS);
        ALL_EXTRAS_INIT.add(URL_S);
        ALL_EXTRAS_INIT.add(URL_SQ);
        ALL_EXTRAS_INIT.add(URL_T);
        ALL_EXTRAS_INIT.add(URL_M);
        ALL_EXTRAS_INIT.add(URL_O);
        ALL_EXTRAS_INIT.add(URL_L);
    }

    public static final Set<String> ALL_EXTRAS = Collections.unmodifiableSet(ALL_EXTRAS_INIT);

    static {
        MIN_EXTRAS_INIT.add(ORIGINAL_FORMAT);
        MIN_EXTRAS_INIT.add(OWNER_NAME);
    }

    public static final Set<String> MIN_EXTRAS = Collections.unmodifiableSet(MIN_EXTRAS_INIT);

    /**
     * No-op constructor.
     */
    private Extras() {
    }

}

package com.prime.launcher.teletextservice.pagestable;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract between the DTV provider and Tv applications. Contains definitions for the
 * URIs and columns.
 * <p> DtvContract defines basic databases of information related to digital TV. It includes:
 * <ul>
 *     <li>streamers: a database that provides information on tuners</li>
 *     <li>players: a database that provides information on players</li>
 * </ul>
 * </p>
 */
public final class PagesDatabase {

    private static final String TAG = "TeletextService_PagesDatabase";

    /**
     * The authority for the DTV provider.
     */
    public static final String AUTHORITY = "com.prime.dtv.provider";

    /*
     * Column definitions for the pages table.
     */
    public static final class Pages implements BaseColumns {
        private Pages() {
        }

        /**
         * Table name.
         */
        public static final String PATH = "pages";

        /**
         * Table version.
         */
        public static final int VERSION = 1;

        // URI ------------------------------------------------------------------------------------
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

        // MIME types -----------------------------------------------------------------------------
        /**
         * The MIME type of a directory of pages.
         */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + AUTHORITY + "." + PATH;

        /**
         * The MIME type of a single pages.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + AUTHORITY + "." + PATH;

        // Columns --------------------------------------------------------------------------------
        /**
         * sub page number
         */
        public static final String COLUMN_PAGE_SUBPAGE = "page_subpage";

        /**
         * page number
         */
        public static final String COLUMN_PAGE = "page";

        /**
         * packet data
         */
        public static final String COLUMN_DATA = "data";
    }
}

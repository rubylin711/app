package com.prime.launcher.teletextservice.decoder;

import android.net.Uri;
import android.provider.BaseColumns;

        public final class DtvContract {

            public static final String AUTHORITY = "com.prime.dtv.provider";
            public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

            private DtvContract() {}

            public static final class Pages implements BaseColumns {
                public static final String TABLE_NAME = "pages";
                public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

                public static final String COLUMN_PAGE_SUBPAGE = "page_subpage";
                public static final String COLUMN_PAGE = "page";
                public static final String COLUMN_DATA = "data";
            }
        }

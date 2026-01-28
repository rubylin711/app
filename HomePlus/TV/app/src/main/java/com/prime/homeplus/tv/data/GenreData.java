package com.prime.homeplus.tv.data;

import android.content.Context;

import com.prime.datastructure.sysdata.FavGroup;
import com.prime.homeplus.tv.R;

import java.util.*;

public class GenreData {
    private static final List<GenreInfo> liveTvGenreList = new ArrayList<>();
    private static final Map<Integer, GenreInfo> liveTvGenreMap = new HashMap<>();

    private static final List<GenreInfo> epgGenreList = new ArrayList<>();
    private static final Map<Integer, GenreInfo> epgGenreMap = new HashMap<>();

    public static final int ID_FAVORITE_CHANNELS = -100;
    public static final int ID_ALL_CHANNELS = -101;

    static {
        /* LiveTV */
        addLiveTvGenre(ID_FAVORITE_CHANNELS, R.string.genre_favorite_channels);
        addLiveTvGenre(ID_ALL_CHANNELS, R.string.genre_all_channels);

        // platform definition
        addLiveTvGenre(0x00100, R.string.genre_welfare_religion);
        addLiveTvGenre(0x00200, R.string.genre_drama_music);
        addLiveTvGenre(0x00400, R.string.genre_news_finance);
        addLiveTvGenre(0x00800, R.string.genre_leisure_knowledge);
        addLiveTvGenre(0x01000, R.string.genre_children_animation);
        addLiveTvGenre(0x02000, R.string.genre_films_series);
        addLiveTvGenre(0x04000, R.string.genre_variety);
        addLiveTvGenre(0x08000, R.string.genre_shopping);
        addLiveTvGenre(0x10000, R.string.genre_foreign_language);
        addLiveTvGenre(0x20000, R.string.genre_hd);
        addLiveTvGenre(0x40000, R.string.genre_sports_others);
        addLiveTvGenre(0x80000, R.string.genre_adult);
        //addLiveTvGenre(0x100000, 0); // '多螢'/'at Home'
        /* LiveTv End */

        /* Epg */
        addEpgGenre(ID_ALL_CHANNELS, R.string.genre_all_channels);

        addEpgGenre(0x00100, R.string.genre_welfare_religion);
        addEpgGenre(0x00200, R.string.genre_drama_music);
        addEpgGenre(0x00400, R.string.genre_news_finance);
        addEpgGenre(0x00800, R.string.genre_leisure_knowledge);
        addEpgGenre(0x01000, R.string.genre_children_animation);
        addEpgGenre(0x02000, R.string.genre_films_series);
        addEpgGenre(0x04000, R.string.genre_variety);
        addEpgGenre(0x08000, R.string.genre_shopping);
        addEpgGenre(0x10000, R.string.genre_foreign_language);
        addEpgGenre(0x20000, R.string.genre_hd);
        addEpgGenre(0x40000, R.string.genre_sports_others);
        addEpgGenre(0x80000, R.string.genre_adult);
        /* Epg End */
    }

    public static int getPrimeGenre(int genreId) {
        if(genreId == 0x00100)
            return FavGroup.GROUP_CNS_PUBLIC_WELFARE_RRLIGION;
        else if(genreId == 0x00200)
            return FavGroup.GROUP_CNS_DRAMA_MUSIC;
        else if(genreId == 0x00400)
            return FavGroup.GROUP_CNS_NEWS_FINANCE;
        else if(genreId == 0x00800)
            return FavGroup.GROUP_CNS_LEISURE_KNOWLEDGE;
        else if(genreId == 0x01000)
            return FavGroup.GROUP_CNS_CHILDREN_ANIMATION;
        else if(genreId == 0x02000)
            return FavGroup.GROUP_CNS_FILMS_SERIES;
        else if(genreId == 0x04000)
            return FavGroup.GROUP_CNS_VARIETY;
        else if(genreId == 0x08000)
            return FavGroup.GROUP_CNS_HOME_SHOPPING;
        else if(genreId == 0x10000)
            return FavGroup.GROUP_CNS_FOREIGN_LANGUAGE_LEARNING;
        else if(genreId == 0x20000)
            return FavGroup.GROUP_CNS_HD;
        else if(genreId == 0x40000)
            return FavGroup.GROUP_CNS_SPORTS_OTHERS;
        else if(genreId == 0x80000)
            return FavGroup.GROUP_CNS_ADULT;
        else
            return FavGroup.ALL_TV_TYPE;
    }

    private static void addLiveTvGenre(int id, int nameResId) {
        GenreInfo genre = new GenreInfo(id, nameResId);
        liveTvGenreList.add(genre);
        liveTvGenreMap.put(id, genre);
    }

    public static int getLiveTvGenreIndexById(int id) {
        for (int i = 0; i < liveTvGenreList.size(); i++) {
            if (liveTvGenreList.get(i).id == id) {
                return i;
            }
        }
        return -1;
    }

    public static List<GenreInfo> getAllLiveTvGenres() {
        return Collections.unmodifiableList(liveTvGenreList);
    }

    public static String getGenreName(Context context, int id) {
        GenreInfo genre = liveTvGenreMap.get(id);
        if (genre != null) {
            return genre.getName(context);
        }
        return null;
    }

    private static void addEpgGenre(int id, int nameResId) {
        GenreInfo genre = new GenreInfo(id, nameResId);
        epgGenreList.add(genre);
        epgGenreMap.put(id, genre);
    }

    public static int getEpgGenreIndexById(int id) {
        for (int i = 0; i < epgGenreList.size(); i++) {
            if (epgGenreList.get(i).id == id) {
                return i;
            }
        }
        return -1;
    }

    public static List<GenreInfo> getAllEpgGenres() {
        return Collections.unmodifiableList(epgGenreList);
    }

    public static class GenreInfo {
        public final int id;
        public final int nameResId;

        public GenreInfo(int id, int nameResId) {
            this.id = id;
            this.nameResId = nameResId;
        }

        public String getName(Context context) {
            return context.getString(nameResId);
        }
    }
}


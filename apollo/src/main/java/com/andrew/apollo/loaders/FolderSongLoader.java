/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Audio.AudioColumns;

import com.andrew.apollo.model.Song;
import com.andrew.apollo.utils.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to query {@link MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}
 * and return songs for a particular folder on a user's device.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class FolderSongLoader extends WrappedAsyncTaskLoader<List<Song>> {

    /**
     * The result
     */
    private final ArrayList<Song> mSongList = Lists.newArrayList();

    /**
     * The {@link Cursor} used to run the query.
     */
    private Cursor mCursor;

    /**
     * The {@link File} of the folder to query.
     */
    private final File mFolder;

    /**
     * Constructor of <code>FolderSongHandler</code>
     *
     * @param context The {@link Context} to use.
     * @param folder The {@link File} for the folder to query.
     */
    public FolderSongLoader(final Context context, final File folder) {
        super(context);
        mFolder = folder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Song> loadInBackground() {
        // Create the Cursor
        mCursor = makeFileSongCursor(getContext(), mFolder);
        // Gather the data
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                // Copy the song Id
                final long id = mCursor.getLong(0);

                // Copy the song name
                final String songName = mCursor.getString(1);

                // Copy the album name
                final String album = mCursor.getString(2);

                // Copy the artist name
                final String artist = mCursor.getString(3);

                // Copy the duration
                final long duration = mCursor.getLong(4);

                // Convert the duration into seconds
                final int durationInSecs = (int) duration / 1000;

                // Create a new song
                final Song song = new Song(id, songName, artist, album, durationInSecs, -1, -1);

                // Add everything up
                mSongList.add(song);
            } while (mCursor.moveToNext());
        }
        // Close the cursor
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }

    /**
     * @param context The {@link Context} to use.
     * @param folder The {@link File} for the folder to query.
     * @return The {@link Cursor} used to run the query.
     */
    public static final Cursor makeFileSongCursor(final Context context, final File folder) {
        // Find songs within the folder.
        final StringBuilder selection = new StringBuilder();
        selection.append(AudioColumns.IS_MUSIC + "=1"); //$NON-NLS-1$
        selection.append(" AND " + MediaColumns.TITLE + "!=''"); //$NON-NLS-1$ //$NON-NLS-2$
        selection.append(" AND " + MediaStore.Audio.AudioColumns.DATA + " LIKE ?"); //$NON-NLS-1$ //$NON-NLS-2$
        return context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
                        /* 0 */
                        MediaStore.Audio.Media._ID,
                        /* 1 */
                        MediaStore.Audio.Media.TITLE,
                        /* 2 */
                        MediaStore.Audio.Media.ALBUM,
                        /* 3 */
                        MediaStore.Audio.Media.ARTIST,
                        /* 4 */
                        MediaStore.Audio.Media.DURATION,
                }, selection.toString(),
                new String[]{folder.toString() + '%'},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }
}
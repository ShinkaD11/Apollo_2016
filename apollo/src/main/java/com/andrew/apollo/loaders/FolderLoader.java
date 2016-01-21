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
import android.provider.MediaStore.Audio.AudioColumns;

import com.andrew.apollo.utils.Lists;
import com.andrew.apollo.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import java.io.File;


/**
 * Used to query {@link MediaStore.Audio.Media.EXTERNAL_CONTENT_URI} and return
 * folders that contain songs on a user's device.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class FolderLoader extends WrappedAsyncTaskLoader<List<File>> {

    /**
     * The result
     */
    private final ArrayList<File> mFolders = Lists.newArrayList();

    /**
     * The {@link Cursor} used to run the query.
     */
    private Cursor mCursor;

    /**
     * Constructor of <code>FolderLoader</code>
     *
     * @param context The {@link Context} to use
     */
    public FolderLoader(final Context context) {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<File> loadInBackground() {
        final HashSet<File> dirs = new HashSet<File>();
        mCursor = makeSongCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                // Copy the song filename and store the folder path.
                File file = new File(mCursor.getString(0));
                dirs.add(file.getAbsoluteFile().getParentFile());
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        mFolders.clear();
        mFolders.addAll(dirs);
        Collections.sort(mFolders, new Comparator<File>(){
            public int compare(File fileA, File fileB) {
                return fileA.getName().compareToIgnoreCase(fileB.getName());
            }
        });
        return mFolders;
    }

    /**
     * Creates the {@link Cursor} used to run the query.
     *
     * @param context The {@link Context} to use.
     * @return The {@link Cursor} used to run the song query.
     */
    public static final Cursor makeSongCursor(final Context context) {
        final StringBuilder mSelection = new StringBuilder();
        mSelection.append(AudioColumns.IS_MUSIC + "=1"); //$NON-NLS-1$
        mSelection.append(" AND " + AudioColumns.TITLE + " != ''"); //$NON-NLS-1$ //$NON-NLS-2$
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        /* 0 */
                        MediaStore.Audio.AudioColumns.DATA
                }, mSelection.toString(), null,
                PreferenceUtils.getInstance(context).getSongSortOrder());
    }
}
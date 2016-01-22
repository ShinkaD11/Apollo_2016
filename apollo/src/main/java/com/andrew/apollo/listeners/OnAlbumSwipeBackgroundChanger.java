/*
 * Copyright (C) 2014 Patrick Stillhart Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.listeners;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Patrick Stillhart <patrick@stillhart.biz> on 26.10.2014.

 * OnTouchListener which moves an image horizontal according to the finger motion
 */
public abstract class OnAlbumSwipeBackgroundChanger implements View.OnTouchListener {

    // How far to pull the cover for trigger
    private static final int THRESHOLD = 150;
    // How far can you pull the cover (this is the multiplier)
    private static final int OPENING = 100;

    private float mDownX;
    private boolean IsNextBackground;
    private ImageView mBackgroundContainer;
    private Drawable mBackgroundPrevious, mBackgroundNext;

    /** Setup the basics
     *
     * @param backgroundContainer The background container in which the arts are drawn
     */
    public OnAlbumSwipeBackgroundChanger(ImageView backgroundContainer) {
        this.mBackgroundContainer = backgroundContainer;
    }

    /** Update the background arts
     *
     * @param previous Art from previous song
     * @param next Art from next song
     */
    public void setImages(Drawable previous, Drawable next){
        this.mBackgroundPrevious = previous;
        this.mBackgroundNext = next;

        if(IsNextBackground) mBackgroundContainer.setImageDrawable(next);
        else mBackgroundContainer.setImageDrawable(previous);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = motionEvent.getX() + 10;
                return true;

            case MotionEvent.ACTION_MOVE:
                // previous
                if (mDownX < motionEvent.getX()) {
                    if (IsNextBackground) {
                        mBackgroundContainer.setImageDrawable(mBackgroundPrevious);
                        IsNextBackground = false;
                    }

                    float x = (float) Math.log(Math.toRadians(motionEvent.getX() - mDownX)) * OPENING;
                    if (x > 1) {
                        view.setTranslationX(x);
                        mBackgroundContainer.setTranslationX(-mBackgroundContainer.getWidth() + x);
                        mBackgroundContainer.setVisibility(View.VISIBLE);
                    } else {
                        resetTranslation(view);
                    }
                    // next
                } else if (mDownX > motionEvent.getX()) {
                    if (!IsNextBackground) {
                        mBackgroundContainer.setImageDrawable(mBackgroundNext);
                        IsNextBackground = true;
                    }

                    float x = (float) Math.log(Math.toRadians(mDownX - motionEvent.getX())) * OPENING;
                    if (x > 1) {
                        view.setTranslationX(-x);
                        mBackgroundContainer.setTranslationX(mBackgroundContainer.getWidth() - x);
                        mBackgroundContainer.setVisibility(View.VISIBLE);
                    } else {
                        resetTranslation(view);
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() - mDownX > THRESHOLD) previous();
                else if (mDownX - motionEvent.getX() > THRESHOLD) next();

                resetTranslation(view);
                return true;

        }

        return false;
    }

    private void resetTranslation(View view) {
        mBackgroundContainer.setVisibility(View.INVISIBLE);
        view.setTranslationX(0);
    }

    /**
     * Gets called when user pulled enough to the right
     */
    public abstract void previous();

    /**
     * Gets called when user pulled enough to the left
     */
    public abstract void next();


}
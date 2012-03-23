/* Copyright 2012 Olie

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package org.andengine.helperclasses;

import java.util.ArrayList;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import android.view.VelocityTracker;

public class LevelSelector extends Entity implements IScrollDetectorListener{
	
	private static int THRESHOLD_FLING_VELOCITY = 250;
	private ArrayList<Sprite> mItems;
	private int mCols, mRows, mPageCount, mCurrentPage;
	private Padding mPadding;
	private Camera mCamera;
	private SurfaceScrollDetector mScrollDetector;
	private VelocityTracker mVelocityTracker;
	private boolean mIsVertical;
	private IPageChangeListener mPageChangeListener;
	
	
	public static class Padding {
		int mPaddingX;
		int mPaddingY;
	
		public Padding(int paddingX, int paddingY) {
			if(paddingX == 0 || paddingY == 0)
				throw new IllegalArgumentException();
			mPaddingX = paddingX;
			mPaddingY = paddingY;
		}

		public int getmPaddingX() {
			return mPaddingX;
		}

		public int getmPaddingY() {
			return mPaddingY;
		}
	}
	
	public LevelSelector(ArrayList<Sprite> items, int cols, int rows, Padding padding, Camera camera, boolean isVerticalPaging) {
		if(items == null || camera == null || padding == null)
			throw new NullPointerException();
		if(cols == 0 || rows == 0 || items.size() == 0)
			throw new IllegalArgumentException();
		
		mItems = items;
		mCols = cols;
		mRows = rows;
		mPadding = padding;
		mCamera = camera;
		mScrollDetector = new SurfaceScrollDetector(this);
		mVelocityTracker = VelocityTracker.obtain();
		mCurrentPage = 0; //start at the zeroth page
		mIsVertical = isVerticalPaging;
		buildLevelSelector();
	}
	
	public LevelSelector(ArrayList<Sprite> items, int cols, int rows, Padding padding, Camera camera) {
		this(items, cols, rows, padding, camera, false); //default padding is 0
	}
	
	public void setmPageChangeListener(IPageChangeListener mPageChangeListener) {
		this.mPageChangeListener = mPageChangeListener;
	}

	private void buildLevelSelector() {

		int column = 0, row = 0;
		for(Sprite item : mItems) {
			if(!mIsVertical) {
				item.setPosition(this.getX() + column * mPadding.getmPaddingX() + (mPageCount * mCamera.getWidth()), this.getY() + row * mPadding.getmPaddingY());
			} else {
				item.setPosition(this.getX() + column * mPadding.getmPaddingX(), this.getY() + row * mPadding.getmPaddingY() + (mPageCount * mCamera.getHeight()));
			}
			this.attachChild(item);
			column += 1;
			if(column == mCols) {
				column = 0;
				row += 1;
				if(row == mRows) {
					mPageCount += 1;
					column = 0;
					row = 0;
				}
			}
		}
	}
	
	public void onTouchEvent(TouchEvent pSceneTouchEvent) {
		mScrollDetector.onTouchEvent(pSceneTouchEvent);
		mVelocityTracker.addMovement(pSceneTouchEvent.getMotionEvent());
	}
	
	@Override
	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
	}

	@Override
	public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
		if(mIsVertical) {
			mCamera.offsetCenter(0, -pDistanceY);
		} else {
			mCamera.offsetCenter(-pDistanceX, 0);
		}
	}

	@Override
	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		
		mVelocityTracker.computeCurrentVelocity(1000);
		float velocity;
		if(mIsVertical) {
			velocity = mVelocityTracker.getYVelocity();
		} else {
			velocity = mVelocityTracker.getXVelocity();
		}
		if(velocity > THRESHOLD_FLING_VELOCITY) {
			if(mCurrentPage != 0) {
				mCurrentPage -= 1;
			}
		} else if(velocity < -THRESHOLD_FLING_VELOCITY) {
			if(mCurrentPage != mPageCount) {
				mCurrentPage += 1;
			}
		} else {
			float cameraPanParameter;
			float cameraMinParameter;
			
			cameraPanParameter = mCamera.getWidth();
			cameraMinParameter = mCamera.getXMin();
			
			if(mIsVertical) {
				cameraPanParameter = mCamera.getHeight();
				cameraMinParameter = mCamera.getYMin();
			}
			
			if(cameraMinParameter > mCurrentPage * cameraPanParameter + cameraPanParameter/2 && mCurrentPage != mPageCount) {
				mCurrentPage += 1;
			} else if(cameraMinParameter < mCurrentPage * cameraPanParameter + cameraPanParameter/2 && mCurrentPage != 0) {
				mCurrentPage -= 1;
			}
		}
		moveToCurrentPage();
		mVelocityTracker.clear();
	}
	
	private void moveToCurrentPage() {
		float displacement = this.mCamera.getWidth() * mCurrentPage - this.mCamera.getXMin();
		if(mIsVertical) {
			displacement = this.mCamera.getHeight() * mCurrentPage - this.mCamera.getYMin();
			mCamera.offsetCenter(0, displacement);
		} else {
			mCamera.offsetCenter(displacement, 0);
		}
		if(mPageChangeListener != null)
			mPageChangeListener.onPageChange(mCurrentPage);
	}
	
	public interface IPageChangeListener {
		public void onPageChange(int pageIndex);
	}
}

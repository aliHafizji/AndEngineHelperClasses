package org.andengine.helperclasses;

import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Slider extends Entity{

	private Sprite mSlider, mThumb;
	private float mValue, mHeight, mWidth;
	private OnSliderValueChangeListener mListener;
	
	public Slider(ITextureRegion sliderTextureRegion, ITextureRegion thumbTextureRegion, VertexBufferObjectManager vertexBufferObjectManager) {
		if(sliderTextureRegion == null || thumbTextureRegion == null)
			throw new NullPointerException("Slider or thumb texture region cannot be null");
		mValue = 0.0f;
		mSlider = new Sprite(0, 0, sliderTextureRegion, vertexBufferObjectManager);
		mThumb = new Sprite(-thumbTextureRegion.getWidth()/2, -thumbTextureRegion.getHeight()/2 + sliderTextureRegion.getHeight()/2, thumbTextureRegion, vertexBufferObjectManager){

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				//restrict the movement of the thumb only on the slider
				float newX = pSceneTouchEvent.getX() - this.getParent().getX();
				if( (newX < (mSlider.getWidth() + mSlider.getX())) && (newX > mSlider.getX()) ) {
					this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2 - this.getParent().getX(), this.getY());
					//find the fraction that the newX is at of the length of the slider
					mValue = (newX/mSlider.getWidth()) * 100;
					if(mListener != null)
						mListener.onSliderValueChanged(mValue);
					return true;
				}
				return false;
			}
			
		};
		mWidth = (mThumb.getWidth() >= mSlider.getWidth()) ? mThumb.getWidth() : mSlider.getWidth();
		mHeight = (mThumb.getHeight() >= mSlider.getHeight()) ? mThumb.getHeight() : mSlider.getHeight();
		this.attachChild(mSlider);
		this.attachChild(mThumb);
	}

	public Sprite getmThumb() {
		return mThumb;
	}
	
	public float getWidth() {
		return mWidth;
	}
	
	public float getHeight() {
		return mHeight;
	}
	
	public void setOnSliderValueChangeListener(OnSliderValueChangeListener sliderValueChangeListener) {
		if(sliderValueChangeListener == null)
			throw new NullPointerException("OnSliderValueChangeListener cannot be null");
		this.mListener = sliderValueChangeListener;
	}
	
	public interface OnSliderValueChangeListener {
		public void onSliderValueChanged(float value);
	}
}

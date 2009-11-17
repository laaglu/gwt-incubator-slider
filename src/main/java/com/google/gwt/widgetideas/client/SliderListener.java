package com.google.gwt.widgetideas.client;

import java.util.EventListener;

public interface SliderListener extends EventListener {
	  /**
	   * This event is fired when a slider starts sliding.
	   */
	  public void onStartSliding(SliderBar slider);

	  /**
	   * This event is fired when a slider stops sliding.
	   */
	  public void onStopSliding(SliderBar slider);

	  /**
	   * This event is fired a slider value changes.
	   * 
	   * @param curValue the current value
	   */
	  public void onValueChanged(SliderBar slider, double curValue);
}

package com.google.gwt.widgetideas.client;

/**
 * A widget that implements this interface sources the events defined by the
 * {@link com.google.gwt.widgetideas.client.SliderListener} interface.
 */
public interface SourcesSliderEvents {
	  /**
	   * Adds a listener interface to receive slider events.
	   * 
	   * @param listener the listener interface to add
	   */
	  void addSliderListener(SliderListener listener);

	  /**
	   * Removes a previously added listener interface.
	   * 
	   * @param listener the listener interface to remove
	   */
	  void removeSliderListener(SliderListener listener);
}

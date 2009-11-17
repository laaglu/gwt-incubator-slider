package com.google.gwt.widgetideas.client;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A helper class for implementers of the SourcesSliderEvents interface. This
 * subclass of {@link ArrayList} assumes that all objects added to it will be of
 * type {@link com.google.gwt.widgetideas.client.SliderListener}.
 */
public class SliderListenerCollection extends ArrayList {
  /**
   * Fires a startSliding event to all listeners.
   * 
   * @param sender the widget sending the event.
   */
  public void fireStartSliding(SliderBar slider) {
    for (Iterator it = iterator(); it.hasNext();) {
      SliderListener listener = (SliderListener) it.next();
      listener.onStartSliding(slider);
    }
  }
  /**
   * Fires a stopSliding event to all listeners.
   * 
   * @param sender the widget sending the event.
   */
  public void fireStopSliding(SliderBar slider) {
    for (Iterator it = iterator(); it.hasNext();) {
      SliderListener listener = (SliderListener) it.next();
      listener.onStopSliding(slider);
    }
  }
  /**
   * Fires a valueChanged event to all listeners.
   * 
   * @param sender the widget sending the event.
   */
  public void fireValueChanged(SliderBar slider, double curValue) {
    for (Iterator it = iterator(); it.hasNext();) {
      SliderListener listener = (SliderListener) it.next();
      listener.onValueChanged(slider, curValue);
    }
  }
}

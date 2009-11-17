package com.google.gwt.widgetideas.client;

/**
 * An adapter to simplify slider event listeners that do not need all events
 * defined on the SliderListener interface.
 */
public class SliderListenerAdapter implements SliderListener {
  public void onStartSliding(SliderBar slider) {
  }

  public void onStopSliding(SliderBar slider) {
  }

  public void onValueChanged(SliderBar slider, double curValue) {
  }
}

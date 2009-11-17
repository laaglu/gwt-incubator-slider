package com.google.gwt.widgetideas.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Abstract base class for slider bars.
 */
public abstract class SliderBar extends FocusPanel implements ResizableWidget, SourcesSliderEvents {
	static {
		// Inject CSS in the document headers
		StyleInjector.inject(Resources.INSTANCE.getHSliderCss().getText());
		StyleInjector.inject(Resources.INSTANCE.getVSliderCss().getText());
	}
  /**
   * The timer used to continue to shift the knob as the user holds down one of
   * the left/right arrow keys. Only IE auto-repeats, so we just keep catching
   * the events.
   */
  protected class KeyTimer extends Timer {
    /**
     * A bit indicating that this is the first run.
     */
    private boolean firstRun = true;

    /**
     * The delay between shifts, which shortens as the user holds down the
     * button.
     */
    private int repeatDelay = 30;

    /**
     * A bit indicating whether we are shifting to a higher or lower value.
     */
    private boolean shiftBottom = false;

    /**
     * The number of steps to shift with each press.
     */
    private int multiplier = 1;

    /**
     * This method will be called when a timer fires. Override it to implement
     * the timer's logic.
     */
    public void run() {
      // Highlight the knob on first run
      if (firstRun) {
        firstRun = false;
        startSliding(true, false);
      }

      // Slide the slider bar
      if (shiftBottom) {
        setCurrentValue(curValue + multiplier * stepSize);
      } else {
        setCurrentValue(curValue - multiplier * stepSize);
      }

      // Repeat this timer until cancelled by keyup event
      schedule(repeatDelay);
    }

    /**
     * Schedules a timer to elapse in the future.
     * 
     * @param delayMillis how long to wait before the timer elapses, in
     *          milliseconds
     * @param shiftRight whether to shift up or not
     * @param multiplier the number of steps to shift
     */
    public void schedule(int delayMillis, boolean shiftRight, int multiplier) {
      firstRun = true;
      this.shiftBottom = shiftRight;
      this.multiplier = multiplier;
      super.schedule(delayMillis);
    }
  }

  /**
   * A formatter used to format the labels displayed in the widget.
   */
  public static class LabelFormatter {
    /**
     * Generate the text to display in each label based on the label's value.
     * 
     * Override this method to change the text displayed within the SliderBar.
     * 
     * @param slider the Slider bar
     * @param value the value the label displays
     * @return the text to display for the label
     */
    protected String formatLabel(SliderBar slider, double value) {
      return (int) (10 * value) / 10.0 + "";
    }
  }
  /**
   * The current value.
   */
  protected double curValue;

  /**
   * The images used with the sliding bar.
   */
  protected AbstractImagePrototype[] images;
  

  /**
   * The knob that slides across the line.
   */
  protected Image knobImage = new Image();

  /**
   * The timer used to continue to shift the knob if the user holds down a key.
   */
  protected KeyTimer keyTimer = new KeyTimer();

  /**
   * The elements used to display labels above the ticks.
   */
  protected List/* <Element> */labelElements = new ArrayList/* <Element> */();

  /**
   * The formatter used to generate label text.
   */
  protected LabelFormatter labelFormatter;

  /**
   * The line that the knob moves over.
   */
  protected Element lineElement;

  /**
   * The offset between the edge of the shell and the line.
   */
  protected int lineOffset;

  /**
   * The maximum slider value.
   */
  protected double maxValue;

  /**
   * The minimum slider value.
   */
  protected double minValue;

  /**
   * The number of labels to show.
   */
  protected int numLabels;

  /**
   * The number of tick marks to show.
   */
  protected int numTicks;

  /**
   * A bit indicating whether or not we are currently sliding the slider bar due
   * to keyboard events.
   */
  protected boolean slidingKeyboard = false;

  /**
   * A collection of widgets to notify on slider events.
   */
  protected SliderListenerCollection sliderListeners;

  /**
   * A bit indicating whether or not we are currently sliding the slider bar due
   * to mouse events.
   */
  protected boolean slidingMouse = false;

  /**
   * The size of the increments between knob positions.
   */
  protected double stepSize;

  /**
   * The base name from which CSS style names for the slider are derived.
   */
  protected String styleBaseName;

  /**
   * The elements used to display tick marks, which are the vertical lines along
   * the slider bar.
   */
  protected List/* <Element> */tickElements = new ArrayList/* <Element> */();

  public SliderBar() {
	  super();
	    sinkEvents(Event.FOCUSEVENTS | Event.KEYEVENTS | Event.ONCLICK
	    	      | Event.MOUSEEVENTS | Event.ONMOUSEWHEEL);
  }
  
  public void addSliderListener(SliderListener listener) {
    if (sliderListeners == null) {
    	sliderListeners = new SliderListenerCollection();
    }
    sliderListeners.add(listener);
  }

  /**
   * Return the current value.
   * 
   * @return the current value
   */
  public double getCurrentValue() {
    return curValue;
  }

  /**
   * Return the label formatter.
   * 
   * @return the label formatter
   */
  public LabelFormatter getLabelFormatter() {
    return labelFormatter;
  }

  /**
   * Return the max value.
   * 
   * @return the max value
   */
  public double getMaxValue() {
    return maxValue;
  }

  /**
   * Return the minimum value.
   * 
   * @return the minimum value
   */
  public double getMinValue() {
    return minValue;
  }

  /**
   * Return the number of labels.
   * 
   * @return the number of labels
   */
  public int getNumLabels() {
    return numLabels;
  }

  /**
   * Return the number of ticks.
   * 
   * @return the number of ticks
   */
  public int getNumTicks() {
    return numTicks;
  }

  /**
   * Return the step size.
   * 
   * @return the step size
   */
  public double getStepSize() {
    return stepSize;
  }

  /**
   * Return the total range between the minimum and maximum values.
   * 
   * @return the total range
   */
  public double getTotalRange() {
    if (minValue > maxValue) {
      return 0;
    } else {
      return maxValue - minValue;
    }
  }

  /**
   * Redraw the progress bar when something changes the layout.
   */
  public void redraw() {
    if (isAttached()) {
      int width = DOM.getElementPropertyInt(getElement(), "clientWidth");
      int height = DOM.getElementPropertyInt(getElement(), "clientHeight");
      onResize(width, height);
    }
  }

  public void removeSliderListener(SliderListener listener) {
    if (sliderListeners != null) {
    	sliderListeners.remove(listener);
    }
  }

  /**
   * Set the current value and fire the onValueChange event.
   * 
   * @param curValue the current value
   */
  public void setCurrentValue(double curValue) {
    setCurrentValue(curValue, true);
  }

  /**
   * Set the current value and optionally fire the onValueChange event.
   * 
   * @param curValue the current value
   * @param fireEvent fire the onValue change event if true
   */
  public void setCurrentValue(double curValue, boolean fireEvent) {
    // Confine the value to the range
    this.curValue = Math.max(minValue, Math.min(maxValue, curValue));
    double remainder = (this.curValue - minValue) % stepSize;
    this.curValue -= remainder;

    // Go to next step if more than halfway there
    if ((remainder > (stepSize / 2))
        && ((this.curValue + stepSize) <= maxValue)) {
      this.curValue += stepSize;
    }

    // Redraw the knob
    drawKnob();

    // Fire the onValueChange event
    if (fireEvent && (sliderListeners != null)) {
      sliderListeners.fireValueChanged(this, getCurrentValue());
    }
  }

  /**
   * Set the label formatter.
   * 
   * @param labelFormatter the label formatter
   */
  public void setLabelFormatter(LabelFormatter labelFormatter) {
    this.labelFormatter = labelFormatter;
  }

  /**
   * Set the max value.
   * 
   * @param maxValue the current value
   */
  public void setMaxValue(double maxValue) {
    this.maxValue = maxValue;
    drawLabels();
    resetCurrentValue();
  }

  /**
   * Set the minimum value.
   * 
   * @param minValue the current value
   */
  public void setMinValue(double minValue) {
    this.minValue = minValue;
    drawLabels();
    resetCurrentValue();
  }

  /**
   * Set the number of labels to show on the line. Labels indicate the value of
   * the slider at that point. Use this method to enable labels.
   * 
   * If you set the number of labels equal to the total range divided by the
   * step size, you will get a properly aligned "jumping" effect where the knob
   * jumps between labels.
   * 
   * Note that the number of labels displayed will be one more than the number
   * you specify, so specify 1 labels to show labels on either end of the line.
   * In other words, numLabels is really the number of slots between the labels.
   * 
   * setNumLabels(0) will disable labels.
   * 
   * @param numLabels the number of labels to show
   */
  public void setNumLabels(int numLabels) {
    this.numLabels = numLabels;
    drawLabels();
  }

  /**
   * Set the number of ticks to show on the line. A tick is a vertical line that
   * represents a division of the overall line. Use this method to enable ticks.
   * 
   * If you set the number of ticks equal to the total range divided by the step
   * size, you will get a properly aligned "jumping" effect where the knob jumps
   * between ticks.
   * 
   * Note that the number of ticks displayed will be one more than the number
   * you specify, so specify 1 tick to show ticks on either end of the line. In
   * other words, numTicks is really the number of slots between the ticks.
   * 
   * setNumTicks(0) will disable ticks.
   * 
   * @param numTicks the number of ticks to show
   */
  public void setNumTicks(int numTicks) {
    this.numTicks = numTicks;
    drawTicks();
  }

  /**
   * Set the step size.
   * 
   * @param stepSize the current value
   */
  public void setStepSize(double stepSize) {
    this.stepSize = stepSize;
    resetCurrentValue();
  }
  
  /**
   * Draw the knob where it is supposed to be relative to the line.
   */
  protected abstract void drawKnob();

  /**
   * Draw the labels along the line.
   */
  protected abstract void drawLabels();

  /**
   * Draw the tick along the line.
   */
  protected abstract void drawTicks();

  /**
   * Format the label to display above the ticks
   * 
   * Override this method in a subclass to customize the format. By default,
   * this method returns the integer portion of the value.
   * 
   * @param value the value at the label
   * @return the text to put in the label
   */
  protected String formatLabel(double value) {
    if (labelFormatter != null) {
      return labelFormatter.formatLabel(this, value);
    } else {
      return (int) (10 * value) / 10.0 + "";
    }
  }

  /**
   * Get the percentage of the knob's position relative to the size of the line.
   * The return value will be between 0.0 and 1.0.
   * 
   * @return the current percent complete
   */
  protected double getKnobPercent() {
    // If we have no range
    if (maxValue <= minValue) {
      return 0;
    }

    // Calculate the relative progress
    double percent = (curValue - minValue) / (maxValue - minValue);
    return Math.max(0.0, Math.min(1.0, percent));
  }

  /**
   * This method is called immediately after a widget becomes attached to the
   * browser's document.
   */
  protected void onLoad() {
    // Reset the position attribute of the parent element
    DOM.setStyleAttribute(getElement(), "position", "relative");
    redraw();
  }

  /**
   * Highlight this widget.
   */
  protected void highlight() {
    String styleName = getStylePrimaryName();
    DOM.setElementProperty(getElement(), "className", styleName + " "
        + styleName + "-focused");
  }

  /**
   * Reset the progress to constrain the progress to the current range and
   * redraw the knob as needed.
   */
  protected void resetCurrentValue() {
    setCurrentValue(getCurrentValue());
  }
  
  /**
   * Start sliding the knob.
   * 
   * @param highlight true to change the style
   * @param fireEvent true to fire the event
   */
  protected abstract void startSliding(boolean highlight, boolean fireEvent);

  /**
   * Stop sliding the knob.
   * 
   * @param unhighlight true to change the style
   * @param fireEvent true to fire the event
   */
  protected abstract void stopSliding(boolean unhighlight, boolean fireEvent);
  
  /**
   * Unhighlight this widget.
   */
  protected void unhighlight() {
    DOM.setElementProperty(getElement(), "className", getStylePrimaryName());
  }
}

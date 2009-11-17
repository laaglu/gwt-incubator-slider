/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.widgetideas.client;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.KeyboardListener;

/**
 * A widget that allows the user to select a value within a range of possible
 * values using a sliding bar that responds to mouse events.
 * 
 * <h3>Keyboard Events</h3>
 * <p>
 * VSliderBar listens for the following key events. Holding down a key will
 * repeat the action until the key is released.
 * <ul class='css'>
 * <li>left arrow - shift left one step</li>
 * <li>right arrow - shift right one step</li>
 * <li>ctrl+left arrow - jump left 10% of the distance</li>
 * <li>ctrl+right arrow - jump right 10% of the distance</li>
 * <li>home - jump to min value</li>
 * <li>end - jump to max value</li>
 * <li>space - jump to middle value</li>
 * </ul>
 * </p>
 * 
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.gwt-VSliderBar-shell { primary style } </li>
 * <li>.gwt-VSliderBar-shell-focused { primary style when focused } </li>
 * <li>.gwt-VSliderBar-shell gwt-VSliderBar-line { the line that the knob moves
 * along } </li>
 * <li>.gwt-VSliderBar-shell gwt-VSliderBar-line-sliding { the line that the knob
 * moves along when sliding } </li>
 * <li>.gwt-VSliderBar-shell .gwt-VSliderBar-knob { the sliding knob } </li>
 * <li>.gwt-VSliderBar-shell .gwt-VSliderBar-knob-sliding { the sliding knob when
 * sliding } </li>
 * <li>.gwt-VSliderBar-shell .gwt-VSliderBar-tick { the ticks along the line }
 * </li>
 * <li>.gwt-VSliderBar-shell .gwt-VSliderBar-label { the text labels along the
 * line } </li>
 * </ul>
 */
public class VSliderBar extends SliderBar {
  /**
   * An {@link ImageBundle} that provides images for {@link VSliderBar}.
   */
  static interface VSliderBarImages extends ImageBundle {
    /**
     * An image used for the sliding knob.
     * 
     * @return a prototype of this image
     */
    AbstractImagePrototype vslider();

    /**
     * An image used for the sliding knob while sliding.
     * 
     * @return a prototype of this image
     */
    AbstractImagePrototype vsliderSliding();
  }
  
  /**
   * Create a slider bar.
   * 
   * @param minValue the minimum value in the range
   * @param maxValue the maximum value in the range
   */
  public VSliderBar(double minValue, double maxValue) {
    this(minValue, maxValue, null);
  }

  /**
   * Create a slider bar.
   * 
   * @param minValue the minimum value in the range
   * @param maxValue the maximum value in the range
   * @param labelFormatter the label formatter
   */
  public VSliderBar(double minValue, double maxValue,
      LabelFormatter labelFormatter) {
    this(minValue, maxValue, labelFormatter,
        null);
  }

  /**
   * Create a slider bar.
   * 
   * @param minValue the minimum value in the range
   * @param maxValue the maximum value in the range
   * @param labelFormatter the label formatter
   * @param images the images to use for the slider
   */
  public VSliderBar(double minValue, double maxValue,
      LabelFormatter labelFormatter, AbstractImagePrototype[] images) {
	    this(minValue, maxValue, labelFormatter,
	            images, "gwt-VSliderBar");
  }
  
  /**
   * Create a slider bar.
   * 
   * @param minValue the minimum value in the range
   * @param maxValue the maximum value in the range
   * @param labelFormatter the label formatter
   * @param images the images to use for the slider
   * @param styleBaseName the base name from which CSS style
   * names for the slider are derived
   */
  public VSliderBar(double minValue, double maxValue,
      LabelFormatter labelFormatter, AbstractImagePrototype[] images, String styleBaseName) {
    super();
    this.minValue = minValue;
    this.maxValue = maxValue;
    if (images != null) {
      this.images = images;
    } else {
      this.images = new AbstractImagePrototype[2];
      VSliderBarImages defaultImages = (VSliderBarImages) GWT.create(VSliderBarImages.class);
      this.images[0] = defaultImages.vslider();
      this.images[1] = defaultImages.vsliderSliding();
    }
    this.styleBaseName = styleBaseName;
    setLabelFormatter(labelFormatter);

    // Create the outer shell
    DOM.setStyleAttribute(getElement(), "position", "relative");
    setStyleName(styleBaseName + "-shell");

    // Create the line
    lineElement = DOM.createDiv();
    DOM.appendChild(getElement(), lineElement);
    DOM.setStyleAttribute(lineElement, "position", "absolute");
    DOM.setElementProperty(lineElement, "className", styleBaseName + "-line");

    // Create the knob
    this.images[0].applyTo(knobImage);
    Element knobElement = knobImage.getElement();
    DOM.appendChild(getElement(), knobElement);
    DOM.setStyleAttribute(knobElement, "position", "absolute");
    DOM.setElementProperty(knobElement, "className", styleBaseName + "-knob");

    // Make this a resizable widget
    ResizableWidgetCollection.get().add(this);
  }

  /**
   * Listen for events that will move the knob.
   * 
   * @param event the event that occurred
   */
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    switch (DOM.eventGetType(event)) {
      // Unhighlight and cancel keyboard events
      case Event.ONBLUR:
        keyTimer.cancel();
        if (slidingMouse) {
          DOM.releaseCapture(getElement());
          slidingMouse = false;
          slideKnob(event);
          stopSliding(true, true);
        } else if (slidingKeyboard) {
          slidingKeyboard = false;
          stopSliding(true, true);
        }
        unhighlight();
        break;

      // Highlight on focus
      case Event.ONFOCUS:
        highlight();
        break;

      // Mousewheel events
      case Event.ONMOUSEWHEEL:
        int velocityY = DOM.eventGetMouseWheelVelocityY(event);
        DOM.eventPreventDefault(event);
        if (velocityY > 0) {
          shiftBottom(1);
        } else {
          shiftTop(1);
        }
        break;

      // Shift left or right on key press
      case Event.ONKEYDOWN:
        if (!slidingKeyboard) {
          int multiplier = 1;
          if (DOM.eventGetCtrlKey(event)) {
            multiplier = (int) (getTotalRange() / stepSize / 10);
          }

          switch (DOM.eventGetKeyCode(event)) {
            case KeyboardListener.KEY_HOME:
              DOM.eventPreventDefault(event);
              setCurrentValue(minValue);
              break;
            case KeyboardListener.KEY_END:
              DOM.eventPreventDefault(event);
              setCurrentValue(maxValue);
              break;
            case KeyboardListener.KEY_UP:
              DOM.eventPreventDefault(event);
              slidingKeyboard = true;
              startSliding(false, true);
              shiftTop(multiplier);
              keyTimer.schedule(400, false, multiplier);
              break;
            case KeyboardListener.KEY_DOWN:
              DOM.eventPreventDefault(event);
              slidingKeyboard = true;
              startSliding(false, true);
              shiftBottom(multiplier);
              keyTimer.schedule(400, true, multiplier);
              break;
            case 32:
              DOM.eventPreventDefault(event);
              setCurrentValue(minValue + getTotalRange() / 2);
              break;
          }
        }
        break;
      // Stop shifting on key up
      case Event.ONKEYUP:
        keyTimer.cancel();
        if (slidingKeyboard) {
          slidingKeyboard = false;
          stopSliding(true, true);
        }
        break;

      // Mouse Events
      case Event.ONMOUSEDOWN:
        setFocus(true);
        slidingMouse = true;
        DOM.setCapture(getElement());
        startSliding(true, true);
        DOM.eventPreventDefault(event);
        slideKnob(event);
        break;
      case Event.ONMOUSEUP:
        if (slidingMouse) {
          DOM.releaseCapture(getElement());
          slidingMouse = false;
          slideKnob(event);
          stopSliding(true, true);
        }
        break;
      case Event.ONMOUSEMOVE:
        if (slidingMouse) {
          slideKnob(event);
        }
        break;
    }
  }

  /**
   * This method is called when the dimensions of the parent element change.
   * Subclasses should override this method as needed.
   * 
   * @param width the new client width of the element
   * @param height the new client height of the element
   */
  public void onResize(int width, int height) {
    // Center the line in the shell
    int lineHeight = DOM.getElementPropertyInt(lineElement, "offsetHeight");
    lineOffset = (height / 2) - (lineHeight / 2);
    DOM.setStyleAttribute(lineElement, "top", lineOffset + "px");

    // Draw the other components
    drawLabels();
    drawTicks();
    drawKnob();
  }

   /**
   * Shift to the left (smaller value).
   * 
   * @param numSteps the number of steps to shift
   */
  public void shiftTop(int numSteps) {
    setCurrentValue(getCurrentValue() - numSteps * stepSize);
  }

  /**
   * Shift to the right (greater value).
   * 
   * @param numSteps the number of steps to shift
   */
  public void shiftBottom(int numSteps) {
    setCurrentValue(getCurrentValue() + numSteps * stepSize);
  }

  /**
   * Draw the knob where it is supposed to be relative to the line.
   */
  protected void drawKnob() {
    // Abort if not attached
    if (!isAttached()) {
      return;
    }

    // Move the knob to the correct position
    Element knobElement = knobImage.getElement();
    int lineHeight = DOM.getElementPropertyInt(lineElement, "offsetHeight");
    int knobHeight = DOM.getElementPropertyInt(knobElement, "offsetHeight");
    int knobTopOffset = (int) (lineOffset + (getKnobPercent() * lineHeight) - (knobHeight / 2));
    knobTopOffset = Math.min(knobTopOffset, lineOffset + lineHeight
        - (knobHeight / 2) - 1);
    DOM.setStyleAttribute(knobElement, "top", knobTopOffset + "px");
  }

  /**
   * Draw the labels along the line.
   */
  protected void drawLabels() {
    // Abort if not attached
    if (!isAttached()) {
      return;
    }

    // Draw the labels
    int lineHeight = DOM.getElementPropertyInt(lineElement, "offsetHeight");
    if (numLabels > 0) {
      // Create the labels or make them visible
      for (int i = 0; i <= numLabels; i++) {
        Element label = null;
        if (i < labelElements.size()) {
          label = (Element) labelElements.get(i);
        } else { // Create the new label
          label = DOM.createDiv();
          DOM.setStyleAttribute(label, "position", "absolute");
          DOM.setStyleAttribute(label, "display", "none");
          DOM.setElementProperty(label, "className", styleBaseName + "-label");
          DOM.appendChild(getElement(), label);
          labelElements.add(label);
        }

        // Set the label text
        double value = minValue + (getTotalRange() * i / numLabels);
        DOM.setStyleAttribute(label, "visibility", "hidden");
        DOM.setStyleAttribute(label, "display", "");
        DOM.setElementProperty(label, "innerHTML", formatLabel(value));

        // Move to the left so the label width is not clipped by the shell
        DOM.setStyleAttribute(label, "top", "0px");

        // Position the label and make it visible
        int labelHeight = DOM.getElementPropertyInt(label, "offsetHeight");
        int labelTopOffset = lineOffset + (lineHeight * i / numLabels)
            - (labelHeight / 2);
        labelTopOffset = Math.min(labelTopOffset, lineOffset + lineHeight
            - labelHeight);
        labelTopOffset = Math.max(labelTopOffset, lineOffset);
        DOM.setStyleAttribute(label, "top", labelTopOffset + "px");
        DOM.setStyleAttribute(label, "visibility", "visible");
      }

      // Hide unused labels
      for (int i = (numLabels + 1); i < labelElements.size(); i++) {
        DOM.setStyleAttribute((Element) labelElements.get(i), "display", "none");
      }
    } else { // Hide all labels
      Iterator it = labelElements.iterator();
      while (it.hasNext()) {
        DOM.setStyleAttribute((Element) it.next(), "display", "none");
      }
    }
  }

  /**
   * Draw the tick along the line.
   */
  protected void drawTicks() {
    // Abort if not attached
    if (!isAttached()) {
      return;
    }

    // Draw the ticks
    int lineHeight = DOM.getElementPropertyInt(lineElement, "offsetHeight");
    if (numTicks > 0) {
      // Create the ticks or make them visible
      for (int i = 0; i <= numTicks; i++) {
        Element tick = null;
        if (i < tickElements.size()) {
          tick = (Element) tickElements.get(i);
        } else { // Create the new tick
          tick = DOM.createDiv();
          DOM.setStyleAttribute(tick, "position", "absolute");
          DOM.setStyleAttribute(tick, "display", "none");
          DOM.setElementProperty(tick, "className", styleBaseName + "-tick");
          DOM.appendChild(getElement(), tick);
          tickElements.add(tick);
        }

        // Position the tick and make it visible
        DOM.setStyleAttribute(tick, "visibility", "hidden");
        DOM.setStyleAttribute(tick, "display", "");
        int tickHeight = DOM.getElementPropertyInt(tick, "offsetHeight");
        int tickTopOffset = lineOffset + (lineHeight * i / numTicks)
            - (tickHeight / 2);
        tickTopOffset = Math.min(tickTopOffset, lineOffset + lineHeight
            - tickHeight);
        DOM.setStyleAttribute(tick, "top", tickTopOffset + "px");
        DOM.setStyleAttribute(tick, "visibility", "visible");
      }

      // Hide unused ticks
      for (int i = (numTicks + 1); i < tickElements.size(); i++) {
        DOM.setStyleAttribute((Element) tickElements.get(i), "display", "none");
      }
    } else { // Hide all ticks
      Iterator it = tickElements.iterator();
      while (it.hasNext()) {
        DOM.setStyleAttribute((Element) it.next(), "display", "none");
      }
    }
  }

  /**
   * Slide the knob to a new location.
   * 
   * @param event the mouse event
   */
  private void slideKnob(Event event) {
    int y = DOM.eventGetClientY(event);
    if (y > 0) {
      int lineHeight = DOM.getElementPropertyInt(lineElement, "offsetHeight");
      int lineTop = getAbsoluteTop2(lineElement);
      //int lineTop = DOM.getAbsoluteTop(lineElement);
      double percent = (double) (y - lineTop) / lineHeight * 1.0;
      setCurrentValue(getTotalRange() * percent + minValue, true);
    }
  }
  
  private native int getAbsoluteTop2(Element elem) /*-{
    var top = 0;
    var curr = elem;
    // This intentionally excludes body which has a null offsetParent.    
    while (curr) {
      top -= curr.scrollTop;
      curr = curr.offsetParent;
    }
    while (elem) {
      top += elem.offsetTop;
      elem = elem.offsetParent;
    }
    return top;
  }-*/;

  /**
   * Start sliding the knob.
   * 
   * @param highlight true to change the style
   * @param fireEvent true to fire the event
   */
  protected void startSliding(boolean highlight, boolean fireEvent) {
    if (highlight) {
      DOM.setElementProperty(lineElement, "className",
        styleBaseName + "-line " + styleBaseName + "-line-sliding");
      DOM.setElementProperty(knobImage.getElement(), "className",
    	styleBaseName + "-knob " + styleBaseName + "-knob-sliding");
      images[1].applyTo(knobImage);
    }
    if (fireEvent && (sliderListeners != null)) {
      sliderListeners.fireStartSliding(this);
    }
  }

  /**
   * Stop sliding the knob.
   * 
   * @param unhighlight true to change the style
   * @param fireEvent true to fire the event
   */
  protected void stopSliding(boolean unhighlight, boolean fireEvent) {
    if (unhighlight) {
      DOM.setElementProperty(lineElement, "className", styleBaseName + "-line");

      DOM.setElementProperty(knobImage.getElement(), "className",
    		  styleBaseName + "-knob");
      images[0].applyTo(knobImage);
    }
    if (fireEvent && (sliderListeners != null)) {
      sliderListeners.fireStopSliding(this);
    }
  }
}

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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A collection of {@link ResizableWidget} that periodically checks the outer
 * dimensions of a widget and redraws it as necessary. Every
 * {@link ResizableWidgetCollection} uses a timer, so consider the cost when
 * adding one.
 * 
 * Typically, a {@link ResizableWidgetCollection} is only needed if you expect
 * your widgets to resize based on window resizing or other events. Fixed sized
 * Widgets do not need to be added to a {@link ResizableWidgetCollection} as
 * they cannot be resized.
 */
public class ResizableWidgetCollection implements WindowResizeListener {
  /**
   * Information about a widgets size.
   */
  private static class ResizableWidgetInfo {
    /**
     * The current clientHeight.
     */
    private int curHeight = 0;

    /**
     * The current clientWidth.
     */
    private int curWidth = 0;

    /**
     * Constructor.
     * 
     * @param widget the widget that will be monitored
     */
    public ResizableWidgetInfo(ResizableWidget widget) {
      curWidth = DOM.getElementPropertyInt(widget.getElement(), "clientWidth");
      curHeight = DOM.getElementPropertyInt(widget.getElement(), "clientHeight");
    }

    /**
     * Set the new dimensions of the widget if they changed.
     * 
     * @param width the new width
     * @param height the new height
     * @return true if the dimensions have changed
     */
    public boolean setClientSize(int width, int height) {
      if (width != curWidth || height != curHeight) {
        this.curWidth = width;
        this.curHeight = height;
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * The default delay between resize checks in milliseconds.
   */
  private static final int DEFAULT_RESIZE_CHECK_DELAY = 400;

  /**
   * A static {@link ResizableWidgetCollection} that can be used in most cases.
   */
  private static ResizableWidgetCollection staticCollection = null;

  /**
   * Get the globally accessible {@link ResizableWidgetCollection}. In most
   * cases, the global collection can be used for all {@link ResizableWidget}s.
   * 
   * @return the global {@link ResizableWidgetCollection}
   */
  public static ResizableWidgetCollection get() {
    if (staticCollection == null) {
      staticCollection = new ResizableWidgetCollection();
    }
    return staticCollection;
  }

  /**
   * The timer used to periodically compare the dimensions of elements to their
   * old dimensions.
   */
  private Timer resizeCheckTimer = new Timer() {
    public void run() {
      // Ignore changes that result from window resize events
      if (windowHeight != Window.getClientHeight()
          || windowWidth != Window.getClientWidth()) {
        windowHeight = Window.getClientHeight();
        windowWidth = Window.getClientWidth();
        schedule(resizeCheckDelay);
        return;
      }

      // Look for elements that have new dimensions
      checkWidgetSize();

      // Start checking again
      if (resizeCheckingEnabled) {
        schedule(resizeCheckDelay);
      }
    }
  };

  /**
   * A hash map of the resizable widgets this collection is checking.
   */
  private HashMap/* <ResizableWidget, ResizableWidgetInfo> */widgets = new HashMap();

  /**
   * The current window height.
   */
  private int windowHeight = 0;

  /**
   * The current window width.
   */
  private int windowWidth = 0;

  /**
   * The delay between resize checks.
   */
  private int resizeCheckDelay = DEFAULT_RESIZE_CHECK_DELAY;

  /**
   * A boolean indicating that resize checking should run.
   */
  private boolean resizeCheckingEnabled = true;

  /**
   * Create a ResizableWidget.
   */
  public ResizableWidgetCollection() {
    this(DEFAULT_RESIZE_CHECK_DELAY);
  }

  /**
   * Constructor.
   * 
   * @param resizeCheckingEnabled false to disable resize checking
   */
  public ResizableWidgetCollection(boolean resizeCheckingEnabled) {
    this(DEFAULT_RESIZE_CHECK_DELAY, false);
  }

  /**
   * Constructor.
   * 
   * @param resizeCheckDelay the delay between checks in milliseconds
   */
  public ResizableWidgetCollection(int resizeCheckDelay) {
    this(resizeCheckDelay, true);
  }

  /**
   * Constructor.
   */
  protected ResizableWidgetCollection(int resizeCheckDelay,
      boolean resizeCheckingEnabled) {
    Window.addWindowResizeListener(this);
    setResizeCheckDelay(resizeCheckDelay);
    if (resizeCheckingEnabled) {
      resizeCheckTimer.schedule(resizeCheckDelay);
    } else {
      this.resizeCheckingEnabled = false;
    }
  }

  /**
   * Add a resizable widget to the collection.
   * 
   * @param widget the resizable widget to add
   */
  public void add(ResizableWidget widget) {
    widgets.put(widget, new ResizableWidgetInfo(widget));
  }

  /**
   * Get the delay between resize checks in milliseconds.
   * 
   * @return the resize check delay
   */
  public int getResizeCheckDelay() {
    return resizeCheckDelay;
  }
  
  /**
   * Check whether or not resize checking is enabled.
   * 
   * @return true is resize checking is enabled
   */
  public boolean isResizeCheckingEnabled() {
    return resizeCheckingEnabled;
  }

  /**
   * Called when the browser window is resized.
   * 
   * @param width the width of the window's client area.
   * @param height the height of the window's client area.
   */
  public void onWindowResized(int width, int height) {
    checkWidgetSize();
  }

  /**
   * Remove a {@link ResizableWidget} from the collection.
   * 
   * @param widget the widget to remove
   */
  public void remove(ResizableWidget widget) {
    widgets.remove(widget);
  }

  /**
   * Set the delay between resize checks in milliseconds.
   * 
   * @param resizeCheckDelay the new delay
   */
  public void setResizeCheckDelay(int resizeCheckDelay) {
    this.resizeCheckDelay = resizeCheckDelay;
  }

  /**
   * Set whether or not resize checking is enabled. If disabled, elements will
   * still be resized on window events, but the timer will not check their
   * dimensions periodically.
   * 
   * @param enabled true to enable the resize checking timer
   */
  public void setResizeCheckingEnabled(boolean enabled) {
    if (enabled && !resizeCheckingEnabled) {
      resizeCheckingEnabled = true;
      resizeCheckTimer.schedule(resizeCheckDelay);
    } else if (!enabled && resizeCheckingEnabled) {
      resizeCheckingEnabled = false;
      resizeCheckTimer.cancel();
    }
  }

  /**
   * Check to see if any Widgets have been resized and call their handlers
   * appropriately.
   */
  private void checkWidgetSize() {
    Iterator it = widgets.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry entry = (Map.Entry) it.next();
      ResizableWidget widget = (ResizableWidget) entry.getKey();
      ResizableWidgetInfo info = (ResizableWidgetInfo) entry.getValue();
      int curWidth = DOM.getElementPropertyInt(widget.getElement(),
          "clientWidth");
      int curHeight = DOM.getElementPropertyInt(widget.getElement(),
          "clientHeight");

      // Call the onResize method only if the widget is attached
      if (info.setClientSize(curWidth, curHeight)) {
        if (widget.isAttached()) {
          widget.onResize(curWidth, curHeight);
        }
      }
    }
  }
}

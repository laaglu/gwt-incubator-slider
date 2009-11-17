package com.google.gwt.widgetideas.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;

public interface Resources extends ClientBundle {
	public static final Resources INSTANCE =  GWT.create(Resources.class);
	@NotStrict
	@Source("HSliderBar.css")
	public CssResource getHSliderCss();
	@NotStrict
	@Source("VSliderBar.css")
	public CssResource getVSliderCss();

}

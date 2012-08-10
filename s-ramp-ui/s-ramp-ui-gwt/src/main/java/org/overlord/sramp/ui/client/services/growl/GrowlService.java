/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.ui.client.services.growl;

import java.util.ArrayList;
import java.util.List;

import org.overlord.sramp.ui.client.services.AbstractService;
import org.overlord.sramp.ui.client.services.IServiceLifecycleListener;
import org.overlord.sramp.ui.client.services.ServiceLifecycleContext;
import org.overlord.sramp.ui.client.widgets.dialogs.GrowlDialog;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
 * Implements the lightweight notification service.
 *
 * @author eric.wittmann@redhat.com
 */
public class GrowlService extends AbstractService implements IGrowlService, ResizeHandler {

	private static final int GROWL_WIDTH = 400;
	private static final int GROWL_HEIGHT = 75;
	private static final int GROWL_MARGIN = 10;

	private List<Growl> activeGrowls = new ArrayList<Growl>();
	private List<Growl> completedGrowls = new ArrayList<Growl>();
	private int growlCounter = 0;
	private int windowWidth;
	private int windowHeight;

	/**
	 * Constructor.
	 */
	public GrowlService() {
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.services.AbstractService#start(org.overlord.sramp.ui.client.services.ServiceLifecycleContext, org.overlord.sramp.ui.client.services.IServiceLifecycleListener)
	 */
	@Override
	public void start(ServiceLifecycleContext context, IServiceLifecycleListener serviceListener) {
		saveCurrentWindowDims();
		Window.addResizeHandler(this);
		super.start(context, serviceListener);
	}

	/**
	 * Called to get the current width and height of the browser window.
	 */
	private void saveCurrentWindowDims() {
		windowWidth = Window.getClientWidth();
		windowHeight = Window.getClientHeight();
	}

	/**
	 * @see org.overlord.sramp.ui.client.services.growl.IGrowlService#growl(java.lang.String, java.lang.String)
	 */
	@Override
	public void growl(String title, String message) {
		final Growl growl = createGrowl(title, message);
		this.activeGrowls.add(growl);
		int growlIndex = this.activeGrowls.size() - 1;
		
		final GrowlDialog dialog = new GrowlDialog(title, message);
		growl.setDialog(dialog);
		dialog.addCloseHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				growl.getAliveTimer().cancel();
				onGrowlClosed(growl);
			}
		});
		Timer aliveTimer = new Timer() {
			@Override
			public void run() {
				dialog.hide();
				onGrowlClosed(growl);
			}
		};
		aliveTimer.schedule(5000);
		growl.setAliveTimer(aliveTimer);
		
		positionAndShowGrowlDialog(dialog, growlIndex);
	}

	/**
	 * Called when a growl is closed, either by the user clicking on the close button
	 * or the alive timer fires.
	 * @param growl
	 */
	protected void onGrowlClosed(final Growl growl) {
		activeGrowls.remove(growl);
		completedGrowls.add(growl);
		growl.setDialog(null);
		repositionGrowls();
	}

	/**
	 * Repositions all of the growls.
	 */
	private void repositionGrowls() {
		for (int growlIndex = 0; growlIndex < activeGrowls.size(); growlIndex++) {
			Growl growl = activeGrowls.get(growlIndex);
			positionAndShowGrowlDialog(growl.getDialog(), growlIndex);
		}
	}

	/**
	 * Positions the growl dialog.
	 * @param dialog the growl dialog/popup
	 * @param growlIndex the growl's position relative to other growls (position in the queue of growls)
	 */
	private void positionAndShowGrowlDialog(GrowlDialog dialog, int growlIndex) {
		// Calculate the growl dialog's position (based on its size and growl index)
		int top = this.windowHeight - ((GROWL_HEIGHT + GROWL_MARGIN) * (growlIndex+1));
		int left = this.windowWidth - GROWL_WIDTH - GROWL_MARGIN;
	    left -= Document.get().getBodyOffsetLeft();
	    top -= Document.get().getBodyOffsetTop();
	    // Position the popup - GWT will use absolute CSS positioning
	    dialog.setPopupPosition(left, top);
	    // Show the dialog (uses the aforementioned absolute CSS positioning)
	    dialog.show();
	    // Now pin the growl to the right using fixed positioning
		int right = GROWL_MARGIN;
		dialog.getElement().getStyle().setPosition(Position.FIXED);
		dialog.getElement().getStyle().setRight(right, Unit.PX);
		dialog.getElement().getStyle().setProperty("left", null);
	}

	/**
	 * Creates a new growl.
	 * @param title
	 * @param message
	 */
	private Growl createGrowl(String title, String message) {
		return new Growl(growlCounter++, title, message);
	}

	/**
	 * @see com.google.gwt.event.logical.shared.ResizeHandler#onResize(com.google.gwt.event.logical.shared.ResizeEvent)
	 */
	@Override
	public void onResize(ResizeEvent event) {
		saveCurrentWindowDims();
	}

}

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
import org.overlord.sramp.ui.client.widgets.dialogs.GrowlDialog;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
 * Implements the lightweight notification service.
 *
 * @author eric.wittmann@redhat.com
 */
public class GrowlService extends AbstractService implements IGrowlService {

	private List<Growl> activeGrowls = new ArrayList<Growl>();
	private List<Growl> completedGrowls = new ArrayList<Growl>();
	private int growlCounter = 0;

	/**
	 * Constructor.
	 */
	public GrowlService() {
	}

	/**
	 * @see org.overlord.sramp.ui.client.services.growl.IGrowlService#growl(java.lang.String, java.lang.String)
	 */
	@Override
	public void growl(String title, String message) {
		final Growl growl = createGrowl(title, message);
		this.activeGrowls.add(growl);
		int growlIndex = this.activeGrowls.size() - 1;
		
		final GrowlDialog dialog = new GrowlDialog(title, message) {
			@Override
			protected void onMouseIn() {
				super.onMouseIn();
				System.out.println("Canceling timer for growl: " + growl.getId());
				growl.getAliveTimer().cancel();
			}
			@Override
			protected void onMouseOut() {
				super.onMouseOut();
				System.out.println("Rescheduling timer for growl: " + growl.getId());
				growl.getAliveTimer().schedule(5000);
			}
		};
		growl.setDialog(dialog);
		// Close handler (user closed the notification)
		dialog.addCloseHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				growl.getAliveTimer().cancel();
				onGrowlClosed(growl);
			}
		});
		
		Timer aliveTimer = createAliveTimer(growl, dialog);
		growl.setAliveTimer(aliveTimer);
		
		positionAndShowGrowlDialog(dialog, growlIndex);
	}

	/**
	 * Creates the alive timer for the growl.  When the alive timer fires, the growl will
	 * automatically close.
	 * @param growl
	 * @param dialog
	 */
	protected Timer createAliveTimer(final Growl growl, final GrowlDialog dialog) {
		Timer aliveTimer = new Timer() {
			@Override
			public void run() {
				dialog.hide();
				onGrowlClosed(growl);
			}
		};
		aliveTimer.schedule(5000);
		return aliveTimer;
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
		// Show the dialog first, but make it invisible (so GWT can do its absolute positioning mojo)
		dialog.getElement().getStyle().setVisibility(Visibility.HIDDEN);
	    dialog.show();

	    // Calculate the growl dialog's position (based on its size and growl index)
		int top = Window.getClientHeight() - ((GrowlConstants.GROWL_HEIGHT + GrowlConstants.GROWL_MARGIN) * (growlIndex+1));
	    top -= Document.get().getBodyOffsetTop();
		int right = GrowlConstants.GROWL_MARGIN;

	    // Now pin the growl to the right using fixed positioning
		dialog.getElement().getStyle().setPosition(Position.FIXED);
		dialog.getElement().getStyle().setTop(top, Unit.PX);
		dialog.getElement().getStyle().setRight(right, Unit.PX);
		dialog.getElement().getStyle().setProperty("left", null);
	    dialog.getElement().getStyle().setVisibility(Visibility.VISIBLE);
	}

	/**
	 * Creates a new growl.
	 * @param title
	 * @param message
	 */
	private Growl createGrowl(String title, String message) {
		return new Growl(growlCounter++, title, message);
	}

}

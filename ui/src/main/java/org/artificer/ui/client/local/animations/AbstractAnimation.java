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
package org.artificer.ui.client.local.animations;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for animations.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractAnimation extends Animation {

	private boolean cancelled = false;
	private boolean running = false;
	private Widget targetWidget;

	/**
	 * Constructor.
	 * @param targetWidget
	 */
	public AbstractAnimation(Widget targetWidget) {
		setTargetWidget(targetWidget);
	}

	/**
	 * @return the cancelled
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * @param cancelled the cancelled to set
	 */
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * @see com.google.gwt.animation.client.Animation#onStart()
	 */
	@Override
	protected final void onStart() {
		setRunning(true);
		setCancelled(false);
		super.onStart();
	}

	/**
	 * @see com.google.gwt.animation.client.Animation#onCancel()
	 */
	@Override
	protected final void onCancel() {
		setRunning(false);
		setCancelled(true);
		doOnCancel();
	}

	/**
	 * Called when the animation is cancelled.
	 */
	protected void doOnCancel() {
	}

	/**
	 * @see com.google.gwt.animation.client.Animation#onComplete()
	 */
	@Override
	protected final void onComplete() {
		super.onComplete();
		setRunning(false);
		setCancelled(false);
		doOnComplete();
	}

	/**
	 * Called when the animation completes.  This method is *not* called when the
	 * animation is cancelled.
	 */
	protected void doOnComplete() {
	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param running the running to set
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * @return the targetWidget
	 */
	public Widget getTargetWidget() {
		return targetWidget;
	}

	/**
	 * @param targetWidget the targetWidget to set
	 */
	public void setTargetWidget(Widget targetWidget) {
		this.targetWidget = targetWidget;
	}

}

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
package org.overlord.sramp.ui.client.local.animations;

import com.google.gwt.user.client.ui.Widget;

/**
 * Animation that will move a widget from one position to another.
 *
 * @author eric.wittmann@redhat.com
 */
public class MoveAnimation extends AbstractAnimation {

	private String property;
	private int from;
	private int to;

	/**
	 * Constructor.
	 * @param targetWidget
	 */
	public MoveAnimation(Widget targetWidget, String property, int from, int to) {
		super(targetWidget);
		setProperty(property);
		setFrom(from);
		setTo(to);
	}

	/**
	 * @see com.google.gwt.animation.client.Animation#onUpdate(double)
	 */
	@Override
	protected void onUpdate(double progress) {
		int distance = getTo() - getFrom();
		int delta = (int) ((distance) * progress);
		int newPos = getFrom() + delta;
		getTargetWidget().getElement().getStyle().setProperty(getProperty(), newPos + "px"); //$NON-NLS-1$
	}

	/**
	 * @see org.overlord.sramp.ui.client.animation.AbstractAnimation#doOnCancel()
	 */
	@Override
	protected void doOnCancel() {
		getTargetWidget().getElement().getStyle().setProperty(getProperty(), getFrom() + "px"); //$NON-NLS-1$
	}

	/**
	 * @see org.overlord.sramp.ui.client.animation.AbstractAnimation#doOnComplete()
	 */
	@Override
	protected void doOnComplete() {
		getTargetWidget().getElement().getStyle().setProperty(getProperty(), getTo() + "px"); //$NON-NLS-1$
	}

	/**
	 * @return the property
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * @param property the property to set
	 */
	public void setProperty(String property) {
		this.property = property;
	}

	/**
	 * @return the from
	 */
	public int getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(int from) {
		this.from = from;
	}

	/**
	 * @return the to
	 */
	public int getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(int to) {
		this.to = to;
	}

}

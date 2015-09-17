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


import com.google.gwt.user.client.ui.Widget;

/**
 * GWT animation used to fade out a widget.
 *
 * @author eric.wittmann@redhat.com
 */
public class FadeOutAnimation extends AbstractAnimation {

	/**
	 * Constructor.
	 * @param targetWidget
	 */
	public FadeOutAnimation(Widget targetWidget) {
		super(targetWidget);
	}

	/**
	 * @see com.google.gwt.animation.client.Animation#onUpdate(double)
	 */
	@Override
	protected void onUpdate(double progress) {
		double cssOpacity = 1.0 - progress;
		getTargetWidget().getElement().getStyle().setOpacity(cssOpacity);
	}

	@Override
	protected void doOnCancel() {
		getTargetWidget().getElement().getStyle().setOpacity(1.0);
	}

}

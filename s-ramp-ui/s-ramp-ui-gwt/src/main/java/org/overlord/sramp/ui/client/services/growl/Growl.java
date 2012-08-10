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

import java.util.Date;

import org.overlord.sramp.ui.client.widgets.dialogs.GrowlDialog;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.Timer;

/**
 * Models a single notification being tracked by the growl service.
 *
 * @author eric.wittmann@redhat.com
 */
public class Growl {
	
	private int id;
	private Date timestamp;
	private String title;
	private String message;
	private Timer aliveTimer;
	private Timer fadeTimer;
	private GrowlDialog dialog;
	private Animation autoCloseAnimation;

	/**
	 * Constructor.
	 * @param id
	 * @param title
	 * @param message
	 */
	public Growl(int id, String title, String message) {
		setId(id);
		setTitle(title);
		setMessage(message);
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the aliveTimer
	 */
	public Timer getAliveTimer() {
		return aliveTimer;
	}

	/**
	 * @return the fadeTimer
	 */
	public Timer getFadeTimer() {
		return fadeTimer;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @param aliveTimer the aliveTimer to set
	 */
	public void setAliveTimer(Timer aliveTimer) {
		this.aliveTimer = aliveTimer;
	}

	/**
	 * @param fadeTimer the fadeTimer to set
	 */
	public void setFadeTimer(Timer fadeTimer) {
		this.fadeTimer = fadeTimer;
	}

	/**
	 * @return the dialog
	 */
	public GrowlDialog getDialog() {
		return dialog;
	}

	/**
	 * @param dialog the dialog to set
	 */
	public void setDialog(GrowlDialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the autoCloseAnimation
	 */
	public Animation getAutoCloseAnimation() {
		return autoCloseAnimation;
	}

	/**
	 * @param autoCloseAnimation the autoCloseAnimation to set
	 */
	public void setAutoCloseAnimation(Animation autoCloseAnimation) {
		this.autoCloseAnimation = autoCloseAnimation;
	}

}

/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.ui.client.local.widgets.bootstrap;

import java.util.Date;

import javax.annotation.PostConstruct;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Like a text box, except it deals with dates and presents the user with
 * a calendar picker widget.
 *
 * @author eric.wittmann@redhat.com
 */
public class DateBox extends TextBox {

    private static final String DEFAULT_DATE_FORMAT = "mm/dd/yyyy"; //$NON-NLS-1$

    private static int cidCounter = 1;
    private static String generateUniqueCid() {
        return "cid-" + cidCounter++; //$NON-NLS-1$
    }

    private String cid;

    /**
     * Constructor.
     */
    public DateBox() {
    }

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    cid = generateUniqueCid();
                    getElement().addClassName(cid);
                    initPicker(cid);
                } else {
                    removePicker(cid);
                }
            }
        });
    }

    /**
     * Initializes the bootstrap-datepicker javascript.
     */
    protected native void initPicker(String cid) /*-{
        var selector = '.' + cid;
        $wnd.jQuery(selector).datepicker({
              autoclose: true,
              todayBtn: true,
              todayHighlight: true
          });
    }-*/;

    /**
     * Removes the bootstrap-datepicker from the DOM and cleans up all events.
     */
    protected native void removePicker(String cid) /*-{
        var selector = '.' + cid;
        $wnd.jQuery(selector).datepicker('remove');
    }-*/;

    /**
     * @return the current value as a {@link Date} or null if empty
     */
    public Date getDateValue() {
        return parseDate(getValue());
    }

    /**
     * Parses the given value as a date using the configured date format.
     * @param value
     */
    private Date parseDate(String value) {
        if (value == null || "".equals(value)) //$NON-NLS-1$
            return null;
        DateTimeFormat format = getFormat();
        return format.parse(value);
    }

    /**
     * @param value the new {@link Date} value
     */
    public void setDateValue(Date value) {
        String v = formatDate(value);
        if (v == null)
            v = ""; //$NON-NLS-1$
        setValue(v);
    }

    /**
     * Formats the date using the configured date format.
     * @param value
     * @return the Date formatted as a string or null if the input is null
     */
    private String formatDate(Date value) {
        if (value == null)
            return null;
        DateTimeFormat format = getFormat();
        return format.format(value);
    }

    /**
     * Gets the format.
     */
    private DateTimeFormat getFormat() {
        String strFmt = DEFAULT_DATE_FORMAT;
        if (getElement().hasAttribute("data-date-format")) { //$NON-NLS-1$
            strFmt = getElement().getAttribute("data-date-format"); //$NON-NLS-1$
        }
        return DateTimeFormat.getFormat(strFmt);
    }

    /**
     * Sets the date format used by this instance and by bootstrap-datepicker for
     * this date box.
     * @param format
     */
    public void setDateFormat(String format) {
        getElement().setAttribute("data-date-format", format); //$NON-NLS-1$
    }

}

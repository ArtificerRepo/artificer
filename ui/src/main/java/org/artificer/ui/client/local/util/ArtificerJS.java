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
package org.artificer.ui.client.local.util;


/**
 * JSNI adapter to get access to the SRAMP global javascript defined in the
 * s-ramp-responsive.js javascript file.
 *
 * @author eric.wittmann@redhat.com
 */
public final class ArtificerJS {

    public static native boolean isMobile() /*-{
        return $wnd.SRAMP.isMobile();
    }-*/;
    public static native boolean isFileInputSupported() /*-{
        return $wnd.SRAMP.isFileInputSupported();
    }-*/;
    public static native void onSwitchToDesktop() /*-{
        $wnd.SRAMP.onSwitchToDesktop();
    }-*/;
    public static native void onSwitchToMobile() /*-{
        $wnd.SRAMP.onSwitchToMobile();
    }-*/;
    public static native void onPageLoad() /*-{
        $wnd.SRAMP.onPageLoad();
    }-*/;
    public static native void onResize() /*-{
        $wnd.SRAMP.onResize();
    }-*/;
}

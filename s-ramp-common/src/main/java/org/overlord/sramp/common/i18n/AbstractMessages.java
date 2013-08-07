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
package org.overlord.sramp.common.i18n;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Base class for all i18n Messages classes in s-ramp projects.
 *
 * @author eric.wittmann@redhat.com
 */
public class AbstractMessages {

    private static Map<Class<? extends AbstractMessages>, ResourceBundle> bundles =
            new HashMap<Class<? extends AbstractMessages>, ResourceBundle>();

    private ResourceBundle bundle;

    /**
     * Constructor.
     */
    public AbstractMessages(Class<? extends AbstractMessages> c) {
        bundle = loadBundle(c);
    }

    /**
     * Loads the resource bundle.
     * @param c
     */
    private ResourceBundle loadBundle(Class<? extends AbstractMessages> c) {
        synchronized (bundles) {
            if (!bundles.containsKey(c)) {
                String pkg = c.getPackage().getName();
                bundles.put(c, PropertyResourceBundle.getBundle(pkg + ".messages", Locale.getDefault(), c.getClassLoader())); //$NON-NLS-1$
            }
            return bundles.get(c);
        }
    }

    /**
     * Look up a message in the i18n resource message bundle by key, then format the
     * message with the given params and return the result.
     * @param key
     * @param params
     */
    public String format(String key, Object ... params) {
        if (bundle.containsKey(key)) {
            String msg = bundle.getString(key);
            return MessageFormat.format(msg, params);
        } else {
            return MessageFormat.format("!!{0}!!", key); //$NON-NLS-1$
        }
    }

}

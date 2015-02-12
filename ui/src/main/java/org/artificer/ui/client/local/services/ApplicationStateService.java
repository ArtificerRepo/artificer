package org.artificer.ui.client.local.services;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

/**
 * Local service responsible for holding application state.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ApplicationStateService {

	private Map<String, Object> state = new HashMap<String, Object>();

	/**
	 * Constructor.
	 */
	public ApplicationStateService() {
	}

	/**
	 * Gets application state by key.
	 * @param key
	 */
	public Object get(String key) {
		return state.get(key);
	}

	/**
	 * Gets the application state by key, returning the given default if
	 * not found in the state map.
	 * @param key
	 * @param defaultValue
	 */
	public Object get(String key, Object defaultValue) {
		Object value = get(key);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * Store some application state by key.
	 * @param key
	 * @param value
	 */
	public void put(String key, Object value) {
		state.put(key, value);
	}
}

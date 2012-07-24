package org.overlord.sramp.ui.client;

import org.overlord.sramp.ui.client.places.DashboardPlace;
import org.overlord.sramp.ui.client.services.IServicesListener;
import org.overlord.sramp.ui.client.services.ServiceList;
import org.overlord.sramp.ui.client.services.Services;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint {

	private Place defaultPlace = new DashboardPlace();
	private SimplePanel appWidget = new SimplePanel();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final IClientFactory clientFactory = GWT.create(IClientFactory.class);
		final EventBus eventBus = clientFactory.getEventBus();
		final PlaceController placeController = clientFactory.getPlaceController();

		Services.init(ServiceList.getRegisteredServices(), new IServicesListener() {
			@Override
			public void onAllServicesStarted() {
				// Start ActivityManager for the main widget with our ActivityMapper
				ActivityMapper activityMapper = new ActivityMapperImpl(clientFactory);
				ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
				activityManager.setDisplay(appWidget);

				// Start PlaceHistoryHandler with our PlaceHistoryMapper
				IPlaceHistoryMapper historyMapper = GWT.create(IPlaceHistoryMapper.class);
				PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
				historyHandler.register(placeController, eventBus, defaultPlace);

				RootPanel.get().add(appWidget);
				// Goes to the place represented on URL else default place
				historyHandler.handleCurrentHistory();
			}
			@Override
			public void onError(Throwable error) {
				// TODO do something more interesting when an error occurs here
				Window.alert("App failure: " + error.getMessage());
			}
		});
	}
}

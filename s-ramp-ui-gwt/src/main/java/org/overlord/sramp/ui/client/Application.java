package org.overlord.sramp.ui.client;

import org.overlord.sramp.ui.client.places.DashboardPlace;
import org.overlord.sramp.ui.client.services.IServicesListener;
import org.overlord.sramp.ui.client.services.ServiceList;
import org.overlord.sramp.ui.client.services.Services;
import org.overlord.sramp.ui.client.services.breadcrumb.IBreadcrumbService;
import org.overlord.sramp.ui.client.services.place.IPlaceService;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.Element;
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
				IPlaceService placeService = Services.getServices().getService(IPlaceService.class);
				IBreadcrumbService breadcrumbService = Services.getServices().getService(IBreadcrumbService.class);
				
				// Start ActivityManager for the main widget with our ActivityMapper
				ActivityMapper activityMapper = new ActivityMapperImpl(clientFactory);
				ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
				activityManager.setDisplay(appWidget);

				// Start PlaceHistoryHandler with our PlaceHistoryMapper
				PlaceHistoryMapper historyMapper = placeService.getPlaceHistoryMapper();
				PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
				historyHandler.register(placeController, eventBus, defaultPlace);
				
				// Add the global breadcrumb panel to the page
				RootPanel breadcrumbWrapperDiv = RootPanel.get("breadcrumb-wrapper");
				breadcrumbWrapperDiv.add(breadcrumbService.getBreadcrumbPanel());

				// Replace the contents of div#content with our application widget
				RootPanel contentDiv = RootPanel.get("content");
				Element contentDivElement = contentDiv.getElement();
				NodeList<Node> childNodes = contentDivElement.getChildNodes();
				for (int i = childNodes.getLength() - 1; i >= 0; i--) {
					contentDivElement.removeChild(childNodes.getItem(i));
				}
				contentDiv.clear();
				contentDiv.add(appWidget);
				
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

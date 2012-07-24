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
package org.overlord.sramp.ui.client;

import org.overlord.sramp.ui.client.places.ArtifactPlace;
import org.overlord.sramp.ui.client.places.BrowsePlace;
import org.overlord.sramp.ui.client.places.DashboardPlace;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

/**
 * A simple place history mapper using the {@link WithTokenizers} annotation to
 * supply the tokenizers for the known places in the application.
 *
 * @author eric.wittmann@redhat.com
 */
@WithTokenizers({DashboardPlace.Tokenizer.class, BrowsePlace.Tokenizer.class, ArtifactPlace.Tokenizer.class})
public interface IPlaceHistoryMapper extends PlaceHistoryMapper {

}

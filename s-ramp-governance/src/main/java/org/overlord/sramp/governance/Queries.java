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
package org.overlord.sramp.governance;

import java.util.ArrayList;
import java.util.List;

public class Queries {

    List<Target> targets;
    List<Query> queries;
    
    public List<Target> getTargets() {
        if (targets==null) targets = new ArrayList<Target>();
        return targets;
    }
    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }
    public List<Query> getQueries() {
        if (queries==null) queries = new ArrayList<Query>();
        return queries;
    }
    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }
}

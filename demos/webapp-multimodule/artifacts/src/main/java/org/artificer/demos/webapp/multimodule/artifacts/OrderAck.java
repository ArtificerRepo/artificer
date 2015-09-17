/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.artificer.demos.webapp.multimodule.artifacts;

public class OrderAck {
    
    private boolean _accepted;
    private String _status;
    
    public boolean isAccepted() {
        return _accepted;
    }
    
    public String getStatus() {
        return _status;
    }
    
    public OrderAck setStatus(String status) {
        _status = status;
        return this;
    }
    
    public OrderAck setAccepted(boolean accepted) {
        _accepted = accepted;
        return this;
    }
    
    @Override
    public String toString() {
        return "OrderAck: accepted=" + _accepted + ", status=" + _status;
    }
    
}

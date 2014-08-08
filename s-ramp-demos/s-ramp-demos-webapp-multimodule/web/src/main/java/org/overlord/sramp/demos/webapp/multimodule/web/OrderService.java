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

package org.overlord.sramp.demos.webapp.multimodule.web;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.overlord.sramp.demos.webapp.multimodule.artifacts.Order;
import org.overlord.sramp.demos.webapp.multimodule.artifacts.OrderAck;

@Path("/orderService")
public class OrderService {
    
    private int totalQuantity = 5;
    
    @POST
    public String submitOrder(@QueryParam("id") Integer id, @QueryParam("quantity") Integer quantity) {
        Order order = new Order().setOrderId(id).setQuantity(quantity);
        
        // Create an order ack
        OrderAck orderAck = new OrderAck();
        // Check the inventory
        // Check quantity on hand and generate the ack
        if (order.getQuantity() > totalQuantity) {
            orderAck.setAccepted(true).setStatus("Order Accepted");
            totalQuantity--;
            
            // "Do stuff" with the Order...
        } else {
            orderAck.setAccepted(false).setStatus("Insufficient Quantity");
        }
        return orderAck.toString();
    }
    
    @GET
    public String totalQuantity() {
        return totalQuantity + "";
    }
}

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

package org.artificer.demos.webapp.multimodule.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.artificer.demos.webapp.multimodule.artifacts.Order;
import org.artificer.demos.webapp.multimodule.artifacts.OrderAck;

public class OrderService extends HttpServlet {
    
    private int idCounter = 0;

    private int totalQuantity = 5;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        Order order = new Order().setOrderId(idCounter++).setQuantity(1);

        // Create an order ack
        OrderAck orderAck = new OrderAck();
        // Check the inventory
        // Check quantity on hand and generate the ack
        if (order.getQuantity() < totalQuantity) {
            orderAck.setAccepted(true).setStatus("Order Accepted");
            totalQuantity--;

            // "Do stuff" with the Order...
        } else {
            orderAck.setAccepted(false).setStatus("Insufficient Quantity");
        }
        PrintWriter out = resp.getWriter();
        out.println(orderAck.toString());
    }
}

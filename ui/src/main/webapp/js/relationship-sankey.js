// Originally based on http://bl.ocks.org/Neilos/584b9a5d44d5fe00f779,
// but *heavily* modified for Artificer (Brett Meyer).

'use strict';

var OPACITY = {
        NODE_DEFAULT: 0.9,
        NODE_FADED: 0.1,
        NODE_HIGHLIGHT: 0.8,
        LINK_DEFAULT: 0.6,
        LINK_FADED: 0.05,
        LINK_HIGHLIGHT: 0.9
    },
    TYPES = ["Asset", "Expense", "Revenue", "Equity", "Liability"],
    TYPE_COLORS = ["#1b9e77", "#d95f02", "#7570b3", "#e7298a", "#66a61e", "#e6ab02", "#a6761d"],
    TYPE_HIGHLIGHT_COLORS = ["#66c2a5", "#fc8d62", "#8da0cb", "#e78ac3", "#a6d854", "#ffd92f", "#e5c494"],
    LINK_COLOR = "#b3b3b3",
    INFLOW_COLOR = "#2E86D1",
    OUTFLOW_COLOR = "#D63028",
    NODE_WIDTH = 36,
    COLLAPSER = {
        RADIUS: NODE_WIDTH / 2,
        SPACING: 2
    },
    OUTER_MARGIN = 10,
    MARGIN = {
        TOP: 2 * (COLLAPSER.RADIUS + OUTER_MARGIN),
        RIGHT: OUTER_MARGIN,
        BOTTOM: OUTER_MARGIN,
        LEFT: OUTER_MARGIN
    },
    TRANSITION_DURATION = 400,
    HEIGHT = 600 - MARGIN.TOP - MARGIN.BOTTOM,
    WIDTH = 1200 - MARGIN.LEFT - MARGIN.RIGHT,
    LAYOUT_INTERATIONS = 32;

var isTransitioning = false,
    selectedNode = "",
    singleClicked = false,
    dragged = false;

var svg, tooltip, path, defs, colorScale, highlightColorScale;

var hideTooltip = function () {
    return tooltip.transition()
        .duration(TRANSITION_DURATION)
        .style("opacity", 0);
},

// Used when temporarily disabling user interactions to allow animations to complete
// Need to disable for 2*, since some animations involve two consecutive transitions.
disableUserInteractions = function (time) {
    isTransitioning = true;
    setTimeout(function(){
        isTransitioning = false;
    }, 2 * TRANSITION_DURATION);
},

showTooltip = function () {
    return tooltip
        .style("left", d3.event.pageX + "px")
        .style("top", d3.event.pageY + 15 + "px")
        .transition()
        .duration(TRANSITION_DURATION)
        .style("opacity", 1);
};

var biHiSankey = d3.biHiSankey();

// Set the biHiSankey diagram properties
biHiSankey
    .nodeWidth(NODE_WIDTH)
    .nodeSpacing(10)
    .linkSpacing(4)
    .arrowheadScaleFactor(0.5) // Specifies that 0.5 of the link's stroke WIDTH should be allowed for the marker at the end of the link.
    .size([WIDTH, HEIGHT]);

function initRelationshipVisualization() {
    colorScale = d3.scale.ordinal().domain(TYPES).range(TYPE_COLORS),
        highlightColorScale = d3.scale.ordinal().domain(TYPES).range(TYPE_HIGHLIGHT_COLORS),

        svg = d3.select("#chart").append("svg")
            .attr("width", WIDTH + MARGIN.LEFT + MARGIN.RIGHT)
            .attr("height", HEIGHT + MARGIN.TOP + MARGIN.BOTTOM)
            .append("g")
            .attr("transform", "translate(" + MARGIN.LEFT + "," + MARGIN.TOP + ")");

    svg.append("g").attr("id", "links");
    svg.append("g").attr("id", "nodes");
    svg.append("g").attr("id", "collapsers");

    tooltip = d3.select("#chart").append("div").attr("id", "tooltip");

    tooltip.style("opacity", 0)
        .append("p")
        .attr("class", "value");

    path = biHiSankey.link().curvature(0.45);

    defs = svg.append("defs");

    defs.append("marker")
        .style("fill", LINK_COLOR)
        .attr("id", "arrowHead")
        .attr("viewBox", "0 0 6 10")
        .attr("refX", "1")
        .attr("refY", "5")
        .attr("markerUnits", "strokeWidth")
        .attr("markerWidth", "1")
        .attr("markerHeight", "1")
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M 0 0 L 1 0 L 6 5 L 1 10 L 0 10 z");

    defs.append("marker")
        .style("fill", OUTFLOW_COLOR)
        .attr("id", "arrowHeadInflow")
        .attr("viewBox", "0 0 6 10")
        .attr("refX", "1")
        .attr("refY", "5")
        .attr("markerUnits", "strokeWidth")
        .attr("markerWidth", "1")
        .attr("markerHeight", "1")
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M 0 0 L 1 0 L 6 5 L 1 10 L 0 10 z");

    defs.append("marker")
        .style("fill", INFLOW_COLOR)
        .attr("id", "arrowHeadOutlow")
        .attr("viewBox", "0 0 6 10")
        .attr("refX", "1")
        .attr("refY", "5")
        .attr("markerUnits", "strokeWidth")
        .attr("markerWidth", "1")
        .attr("markerHeight", "1")
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M 0 0 L 1 0 L 6 5 L 1 10 L 0 10 z");
}

function updateRelationshipVisualization() {
    var link, linkEnter, node, nodeEnter, collapser, collapserEnter;

    function dragmove(node) {
        var oldX = node.x;
        var oldY = node.y;
        node.x = Math.max(0, Math.min(WIDTH - node.width, d3.event.x));
        node.y = Math.max(0, Math.min(HEIGHT - node.height, d3.event.y));
        if (Math.abs(oldX - node.x) > 1 || Math.abs(oldY - node.y) > 1) {
            // 'dragged = true' will block other 'click' actions from occurring.  However, without anticipating a small
            // amount of moment, as most of us do when we click, add a bit of tolerance.
            dragged = true;
        }
        d3.select(this).attr("transform", "translate(" + node.x + "," + node.y + ")");
        biHiSankey.relayout();
        svg.selectAll(".node").selectAll("rect").attr("height", function (d) { return d.height; });
        link.attr("d", path);
    }

    function containChildren(node) {
        node.children.forEach(function (child) {
            child.state = "contained";
            child.parent = this;
            child._parent = null;
            containChildren(child);
        }, node);
    }

    function expand(node) {
        node.state = "expanded";
        node.children.forEach(function (child) {
            child.state = "collapsed";
            child._parent = this;
            child.parent = null;
            containChildren(child);
        }, node);
    }

    function collapse(node) {
        node.state = "collapsed";
        containChildren(node);
    }

    function restoreLinksAndNodes() {
        link
            .style("stroke", LINK_COLOR)
            .style("marker-end", function () { return 'url(#arrowHead)'; })
            .transition()
            .duration(TRANSITION_DURATION)
            .style("opacity", OPACITY.LINK_DEFAULT);

        node
            .selectAll("rect")
            .style("fill", function (d) {
                d.color = colorScale(d.type.replace(/ .*/, ""));
                return d.color;
            })
            .style("stroke", function (d) {
                return d3.rgb(colorScale(d.type.replace(/ .*/, ""))).darker(0.1);
            })
            .style("fill-opacity", OPACITY.NODE_DEFAULT);

        node.filter(function (n) { return n.state === "collapsed"; })
            .transition()
            .duration(TRANSITION_DURATION)
            .style("opacity", OPACITY.NODE_DEFAULT);
    }

    function showHideChildren(node) {
        if (node.children.length > 0) {
            disableUserInteractions();

            hideTooltip();
            if (node.state === "collapsed") {
                expand(node);
            }
            else {
                collapse(node);
            }

            biHiSankey.relayout();
            updateRelationshipVisualization();
            link.attr("d", path);
            restoreLinksAndNodes();
        }
    }

    function highlightConnected(g) {
        link.filter(function (d) { return d.source === g; })
            .style("marker-end", function () { return 'url(#arrowHeadInflow)'; })
            .style("stroke", OUTFLOW_COLOR)
            .style("opacity", OPACITY.LINK_DEFAULT);

        link.filter(function (d) { return d.target === g; })
            .style("marker-end", function () { return 'url(#arrowHeadOutlow)'; })
            .style("stroke", INFLOW_COLOR)
            .style("opacity", OPACITY.LINK_DEFAULT);
    }

    function fadeUnconnected(g) {
        link.filter(function (d) { return d.source !== g && d.target !== g; })
            .style("marker-end", function () { return 'url(#arrowHead)'; })
            .transition()
            .duration(TRANSITION_DURATION)
            .style("opacity", OPACITY.LINK_FADED);

        node.filter(function (d) {
            return (d.name === g.name) ? false : !biHiSankey.connected(d, g);
        }).transition()
            .duration(TRANSITION_DURATION)
            .style("opacity", OPACITY.NODE_FADED);
    }

    link = svg.select("#links").selectAll("path.link")
        .data(biHiSankey.visibleLinks(), function (d) { return d.id; });

    link.transition()
        .duration(TRANSITION_DURATION)
        .style("stroke-WIDTH", function (d) { return Math.max(1, d.thickness); })
        .attr("d", path)
        .style("opacity", OPACITY.LINK_DEFAULT);


    link.exit().remove();


    linkEnter = link.enter().append("path")
        .attr("class", "link")
        .style("fill", "none");

    linkEnter.on('mouseenter', function (d) {
        if (!isTransitioning && selectedNode == "") {
            showTooltip().select(".value").text(function () {
                if (d.direction > 0) {
                    return d.source.name + " → " + d.target.name + "\nrelationship: " + d.label;
                }
                return d.target.name + " ← " + d.source.name + "\nrelationship: " + d.label;
            });

            d3.select(this)
                .transition()
                .duration(TRANSITION_DURATION / 2)
                .style("opacity", OPACITY.LINK_HIGHLIGHT);
        }
    });

    linkEnter.on('mouseleave', function () {
        if (!isTransitioning && selectedNode == "") {
            hideTooltip();

            d3.select(this)
                .transition()
                .duration(TRANSITION_DURATION / 2)
                .style("opacity", OPACITY.LINK_DEFAULT);
        }
    });

    linkEnter.sort(function (a, b) { return b.thickness - a.thickness; })
        .classed("leftToRight", function (d) {
            return d.direction > 0;
        })
        .classed("rightToLeft", function (d) {
            return d.direction < 0;
        })
        .style("marker-end", function () {
            return 'url(#arrowHead)';
        })
        .style("stroke", LINK_COLOR)
        .style("opacity", 0)
        .transition()
        .delay(TRANSITION_DURATION)
        .duration(TRANSITION_DURATION)
        .attr("d", path)
        .style("stroke-WIDTH", function (d) { return Math.max(1, d.thickness); })
        .style("opacity", OPACITY.LINK_DEFAULT);


    node = svg.select("#nodes").selectAll(".node")
        .data(biHiSankey.collapsedNodes(), function (d) { return d.id; });


    node.transition()
        .duration(TRANSITION_DURATION)
        .attr("transform", function (d) { return "translate(" + d.x + "," + d.y + ")"; })
        .style("opacity", OPACITY.NODE_DEFAULT)
        .select("rect")
        .style("fill", function (d) {
            d.color = colorScale(d.type.replace(/ .*/, ""));
            return d.color;
        })
        .style("stroke", function (d) { return d3.rgb(colorScale(d.type.replace(/ .*/, ""))).darker(0.1); })
        .style("stroke-WIDTH", "1px")
        .attr("height", function (d) { return d.height; })
        .attr("width", biHiSankey.nodeWidth());


    node.exit()
        .transition()
        .duration(TRANSITION_DURATION)
        .attr("transform", function (d) {
            var collapsedAncestor, endX, endY;
            collapsedAncestor = d.ancestors.filter(function (a) {
                return a.state === "collapsed";
            })[0];
            endX = collapsedAncestor ? collapsedAncestor.x : d.x;
            endY = collapsedAncestor ? collapsedAncestor.y : d.y;
            return "translate(" + endX + "," + endY + ")";
        })
        .remove();


    nodeEnter = node.enter().append("g").attr("class", "node");

    nodeEnter
        .attr("transform", function (d) {
            var startX = d._parent ? d._parent.x : d.x,
                startY = d._parent ? d._parent.y : d.y;
            return "translate(" + startX + "," + startY + ")";
        })
        .style("opacity", 1e-6)
        .transition()
        .duration(TRANSITION_DURATION)
        .style("opacity", OPACITY.NODE_DEFAULT)
        .attr("transform", function (d) { return "translate(" + d.x + "," + d.y + ")"; });

    nodeEnter.append("text");
    nodeEnter.append("rect")
        .style("fill", function (d) {
            d.color = colorScale(d.type.replace(/ .*/, ""));
            return d.color;
        })
        .style("stroke", function (d) {
            return d3.rgb(colorScale(d.type.replace(/ .*/, ""))).darker(0.1);
        })
        .style("stroke-WIDTH", "1px")
        .attr("height", function (d) { return d.height; })
        .attr("width", biHiSankey.nodeWidth());

    node.on("mouseenter", function (g) {
        if (!isTransitioning) {
            //tooltip
            //    .style("left", g.x + MARGIN.LEFT + "px")
            //    .style("top", g.y + g.height + MARGIN.TOP + 15 + "px")
            //    .transition()
            //    .duration(TRANSITION_DURATION)
            //    .style("opacity", 1).select(".value")
            //    .text(function () {
            //        var additionalInstructions = g.children.length ? "\n(Double click to expand)" : "";
            //        return g.name + additionalInstructions;
            //    });
            if (selectedNode == "") {
                // if a node is not currently clicked/selected, highlight
                highlightNode(g);
            }
        }
    });

    node.on("mouseleave", function () {
        if (!isTransitioning) {
            //hideTooltip();
            if (selectedNode == "") {
                // if a node is not currently clicked/selected, un-highlight
                restoreLinksAndNodes();
            }
        }
    });

    node.on("click", function (g) {
        if (dragged) {
            dragged = false;
        } else if (!isTransitioning) {
            // We want to simultaneously support both click and dblclick.
            if (singleClicked) {
                // double clicked
                singleClicked = false;
                showHideChildren(g);
            } else {
                singleClicked = true;
                setTimeout(function() {
                    if (singleClicked) {
                        singleClicked = false;

                        restoreLinksAndNodes();

                        if (g.id == selectedNode) {
                            // unselect
                            selectedNode = "";
                            $("#chart-instructions").html("");
                        } else {
                            selectedNode = g.id;
                            highlightNode(g);
                            $("#chart-instructions").html("Selected artifact: <strong>" + g.name + "</strong>. <a href=\"/artificer-ui/index.html#details;uuid=" + g.id + "\" target=\"_blank\">Click here</a> for its details view. Re-click the node to un-select.");
                        }
                    }
                }, 200);
            }
        }
    });

    function highlightNode(g) {
        highlightConnected(g);
        fadeUnconnected(g);

        d3.select(this).select("rect")
            .style("fill", function (d) {
                d.color = d.netFlow > 0 ? INFLOW_COLOR : OUTFLOW_COLOR;
                return d.color;
            })
            .style("stroke", function (d) {
                return d3.rgb(d.color).darker(0.1);
            })
            .style("fill-opacity", OPACITY.LINK_DEFAULT);
    }

    // allow nodes to be dragged to new positions
    node.call(d3.behavior.drag()
        .origin(function (d) { return d; })
        .on("dragstart", function () {  this.parentNode.appendChild(this); })
        .on("drag", dragmove));

    // add in the text for the nodes
    node.filter(function (d) { return d.value !== 0; })
        .select("text")
        .attr("x", -6)
        .attr("y", function (d) { return d.height / 2; })
        .attr("dy", ".35em")
        .attr("text-anchor", "end")
        .attr("transform", null)
        .text(function (d) { return d.name; })
        .filter(function (d) { return d.x < WIDTH / 2; })
        .attr("x", 6 + biHiSankey.nodeWidth())
        .attr("text-anchor", "start");


    collapser = svg.select("#collapsers").selectAll(".collapser")
        .data(biHiSankey.expandedNodes(), function (d) { return d.id; });


    collapserEnter = collapser.enter().append("g").attr("class", "collapser");

    collapserEnter.append("circle")
        .attr("r", COLLAPSER.RADIUS)
        .style("fill", function (d) {
            d.color = colorScale(d.type.replace(/ .*/, ""));
            return d.color;
        });

    collapserEnter
        .style("opacity", OPACITY.NODE_DEFAULT)
        .attr("transform", function (d) {
            return "translate(" + (d.x + d.width / 2) + "," + (d.y + COLLAPSER.RADIUS) + ")";
        });

    collapserEnter.on("dblclick", function(node) {
        if (!isTransitioning) {
            showHideChildren(node);
        }
    });

    collapser.select("circle")
        .attr("r", COLLAPSER.RADIUS);

    collapser.transition()
        .delay(TRANSITION_DURATION)
        .duration(TRANSITION_DURATION)
        .attr("transform", function (d, i) {
            return "translate("
                + (COLLAPSER.RADIUS + i * 2 * (COLLAPSER.RADIUS + COLLAPSER.SPACING))
                + ","
                + (-COLLAPSER.RADIUS - OUTER_MARGIN)
                + ")";
        });

    collapser.on("mouseenter", function (g) {
        if (!isTransitioning && selectedNode == "") {
            showTooltip().select(".value")
                .text(function () {
                    return g.name + "\n(Double click to collapse)";
                });

            var highlightColor = highlightColorScale(g.type.replace(/ .*/, ""));

            d3.select(this)
                .style("opacity", OPACITY.NODE_HIGHLIGHT)
                .select("circle")
                .style("fill", highlightColor);

            node.filter(function (d) {
                return d.ancestors.indexOf(g) >= 0;
            }).style("opacity", OPACITY.NODE_HIGHLIGHT)
                .select("rect")
                .style("fill", highlightColor);
        }
    });

    collapser.on("mouseleave", function (g) {
        if (!isTransitioning && selectedNode == "") {
            hideTooltip();

            d3.select(this)
                .style("opacity", OPACITY.NODE_DEFAULT)
                .select("circle")
                .style("fill", function (d) {
                    return d.color;
                });

            node.filter(function (d) {
                return d.ancestors.indexOf(g) >= 0;
            }).style("opacity", OPACITY.NODE_DEFAULT)
                .select("rect")
                .style("fill", function (d) {
                    return d.color;
                });
        }
    });

    collapser.exit().remove();

}

var relationshipNodes = []
var relationshipLinks = []

window.addRelationshipsGraphNode = function addRelationshipsGraphNode(type, id, parent, name) {
    relationshipNodes.push({"type":type, "id":id, "parent":parent, "name":name});
};

window.addRelationshipsGraphLink = function addRelationshipsGraphLink(source, target, value, label) {
    relationshipLinks.push({"source":source, "target":target, "value":value, "label":label});
};

window.buildRelationshipsGraph = function buildRelationshipsGraph() {
     initRelationshipVisualization();

     biHiSankey
         .nodes(relationshipNodes)
         .links(relationshipLinks)
         .initializeNodes(function (node) {
            node.state = node.parent ? "contained" : "collapsed";
         })
        .layout(LAYOUT_INTERATIONS);

     disableUserInteractions();

     updateRelationshipVisualization();
 };

//var relationshipNodes = [
//    {"type":"WsdlDocument","id":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","parent":null,"name":"sample.wsdl (WsdlDocument)"},
//    {"type":"XsdDocument","id":"ca4e76a9-2ea5-48b5-ab89-40177ba1eb71","parent":null,"name":"sample.xsd (XsdDocument)"},
//    {"type":"ElementDeclaration","id":"26c90a9a-9668-491a-aac3-9871e45bc83d","parent":"ca4e76a9-2ea5-48b5-ab89-40177ba1eb71","name":"extInput (ElementDeclaration)"},
//    {"type":"Part","id":"19b8ead6-ec26-4351-beb8-9b1fed1d0043","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"parameters (Part)"},
//    {"type":"Message","id":"fffd3364-4306-40d4-9cb5-bc925121a427","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"findRequest (Message)"},
//    {"type":"OperationInput","id":"88f97011-89ff-4cf2-b25a-a3f717262554","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"findRequest (OperationInput)"},
//    {"type":"Part","id":"31d433b6-c73c-4faa-b322-eeef922e9d7b","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"message (Part)"},
//    {"type":"Fault","id":"f295ddde-d1b7-402a-9553-4f1fe6c62885","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"unknownFault (Fault)"},
//    {"type":"Message","id":"ba779683-cb02-4ddd-a016-fe8a0ac354a4","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"faultMessage (Message)"},
//    {"type":"Fault","id":"e749166b-3569-4874-a0f6-c48ac6f9c903","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"errorFault (Fault)"},
//    {"type":"SoapBinding","id":"af32846f-43ca-4662-84d3-a7baae0362d8","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"soap:binding (SoapBinding)"},
//    {"type":"BindingOperationInput","id":"742b3e6a-9da8-45aa-888b-a5d611f801e8","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"findRequest (BindingOperationInput)"},
//    {"type":"BindingOperationOutput","id":"cfb722c3-0a9c-4f19-8139-3233aa9f3ce3","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"findResponse (BindingOperationOutput)"},
//    {"type":"BindingOperationFault","id":"ee419a4b-c8a1-4b51-98cf-8143bb5bde08","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"errorFault (BindingOperationFault)"},
//    {"type":"BindingOperationFault","id":"cfbdf2ec-7542-48c2-b293-45a4da1227f3","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"unknownFault (BindingOperationFault)"},
//    {"type":"BindingOperation","id":"570fa370-fca9-4177-8282-7084305839dd","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"find (BindingOperation)"},
//    {"type":"SoapAddress","id":"a3aba4a7-2d7a-4b5b-8654-138139b04c6e","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"soap:address (SoapAddress)"},
//    {"type":"WsdlService","id":"5da48602-18bb-4ad2-99de-f5dd3dd6ff63","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"SampleService (WsdlService)"},
//    {"type":"Port","id":"8d6acb17-017c-4daa-a50a-9599693847f7","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"SamplePort (Port)"},
//    {"type":"Binding","id":"d17b273f-cb0f-4038-8acb-e94aa6f0892b","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"SampleBinding (Binding)"},
//    {"type":"PortType","id":"8ef0a94b-c244-4ca9-813b-d9644dfd1e25","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"SamplePortType (PortType)"},
//    {"type":"Operation","id":"8408eb37-ad25-4421-8d43-563042fe449c","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"find (Operation)"},
//    {"type":"OperationOutput","id":"4da06277-d2cf-4091-b338-16d86d54ce20","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"findResponse (OperationOutput)"},
//    {"type":"Message","id":"15092307-5663-440d-bd3f-15497c05232b","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"findResponse (Message)"},
//    {"type":"Part","id":"f5e94ff8-5486-44a7-852d-e065329960b8","parent":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","name":"parameters (Part)"},
//    {"type":"ComplexTypeDeclaration","id":"f61ba65d-6ac2-48cc-9cc2-f167edd1e694","parent":"ca4e76a9-2ea5-48b5-ab89-40177ba1eb71","name":"extOutputType (ComplexTypeDeclaration)"}
//]
//
//var relationshipLinks = [
//    {"source":"8f3bdb37-6e3a-4209-9b76-ab39f8af0478","target":"ca4e76a9-2ea5-48b5-ab89-40177ba1eb71","value":1, "label":"importedXsds"},
//    {"source":"19b8ead6-ec26-4351-beb8-9b1fed1d0043","target":"26c90a9a-9668-491a-aac3-9871e45bc83d","value":1, "label":"element"},
//    {"source":"fffd3364-4306-40d4-9cb5-bc925121a427","target":"19b8ead6-ec26-4351-beb8-9b1fed1d0043","value":1, "label":"part"},
//    {"source":"88f97011-89ff-4cf2-b25a-a3f717262554","target":"fffd3364-4306-40d4-9cb5-bc925121a427","value":1, "label":"message"},
//    {"source":"f295ddde-d1b7-402a-9553-4f1fe6c62885","target":"ba779683-cb02-4ddd-a016-fe8a0ac354a4","value":1, "label":"message"},
//    {"source":"ba779683-cb02-4ddd-a016-fe8a0ac354a4","target":"31d433b6-c73c-4faa-b322-eeef922e9d7b","value":1, "label":"part"},
//    {"source":"e749166b-3569-4874-a0f6-c48ac6f9c903","target":"ba779683-cb02-4ddd-a016-fe8a0ac354a4","value":1, "label":"message"},
//    {"source":"570fa370-fca9-4177-8282-7084305839dd","target":"8408eb37-ad25-4421-8d43-563042fe449c","value":1, "label":"operation"},
//    {"source":"570fa370-fca9-4177-8282-7084305839dd","target":"742b3e6a-9da8-45aa-888b-a5d611f801e8","value":1, "label":"input"},
//    {"source":"570fa370-fca9-4177-8282-7084305839dd","target":"ee419a4b-c8a1-4b51-98cf-8143bb5bde08","value":1, "label":"fault"},
//    {"source":"570fa370-fca9-4177-8282-7084305839dd","target":"cfbdf2ec-7542-48c2-b293-45a4da1227f3","value":1, "label":"fault"},
//    {"source":"570fa370-fca9-4177-8282-7084305839dd","target":"cfb722c3-0a9c-4f19-8139-3233aa9f3ce3","value":1, "label":"output"},
//    {"source":"5da48602-18bb-4ad2-99de-f5dd3dd6ff63","target":"8d6acb17-017c-4daa-a50a-9599693847f7","value":1, "label":"port"},
//    {"source":"8d6acb17-017c-4daa-a50a-9599693847f7","target":"a3aba4a7-2d7a-4b5b-8654-138139b04c6e","value":1, "label":"extension"},
//    {"source":"8d6acb17-017c-4daa-a50a-9599693847f7","target":"d17b273f-cb0f-4038-8acb-e94aa6f0892b","value":1, "label":"binding"},
//    {"source":"d17b273f-cb0f-4038-8acb-e94aa6f0892b","target":"8ef0a94b-c244-4ca9-813b-d9644dfd1e25","value":1, "label":"portType"},
//    {"source":"d17b273f-cb0f-4038-8acb-e94aa6f0892b","target":"af32846f-43ca-4662-84d3-a7baae0362d8","value":1, "label":"extension"},
//    {"source":"d17b273f-cb0f-4038-8acb-e94aa6f0892b","target":"570fa370-fca9-4177-8282-7084305839dd","value":1, "label":"bindingOperation"},
//    {"source":"8ef0a94b-c244-4ca9-813b-d9644dfd1e25","target":"8408eb37-ad25-4421-8d43-563042fe449c","value":1, "label":"operation"},
//    {"source":"8408eb37-ad25-4421-8d43-563042fe449c","target":"88f97011-89ff-4cf2-b25a-a3f717262554","value":1, "label":"input"},
//    {"source":"8408eb37-ad25-4421-8d43-563042fe449c","target":"4da06277-d2cf-4091-b338-16d86d54ce20","value":1, "label":"output"},
//    {"source":"8408eb37-ad25-4421-8d43-563042fe449c","target":"e749166b-3569-4874-a0f6-c48ac6f9c903","value":1, "label":"fault"},
//    {"source":"8408eb37-ad25-4421-8d43-563042fe449c","target":"f295ddde-d1b7-402a-9553-4f1fe6c62885","value":1, "label":"fault"},
//    {"source":"4da06277-d2cf-4091-b338-16d86d54ce20","target":"15092307-5663-440d-bd3f-15497c05232b","value":1, "label":"message"},
//    {"source":"15092307-5663-440d-bd3f-15497c05232b","target":"f5e94ff8-5486-44a7-852d-e065329960b8","value":1, "label":"part"},
//    {"source":"f5e94ff8-5486-44a7-852d-e065329960b8","target":"f61ba65d-6ac2-48cc-9cc2-f167edd1e694","value":1, "label":"type"}
//]
//
//buildRelationshipsGraph();
var ForceDirected = (function () {
    function ForceDirected(width = 975, height = 610, radius = 5) {
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.init();
        this.drawGraphs();
    }

    ForceDirected.prototype.init = function () {
        var self = this;
        //975 610
        self.svg = d3.create("svg").attr("viewBox", [0, 0, this.width, this.height]).attr("class", 'ForceDirected');
        //append link array

        self.link = self.svg.append("g")
            .attr("class", "links")
            .selectAll(".link")

        self.path = self.svg
            .selectAll(".edgepath")

        self.node = self.svg.append("g")
            .attr("class", "nodes")
            .selectAll(".node")


        self.color = d3.scaleOrdinal(d3.schemeCategory10).domain(["2", "1", "3", "4"]);

        self.simulation = d3.forceSimulation()
            .force("link", d3.forceLink().id(function (d) {
                    return d.id;
                })
                    // .distance(10)
                    .strength(2)
            )

            //     .distance(function(d) {
            //     return d.value;
            // }).strength(0.2)
            .force("charge", d3.forceManyBody()
                .strength(function (d) {
                    return 1;
                }))
            .force("center", d3.forceCenter(this.width / 2, this.height / 2))
            .force('collide', d3.forceCollide(function (d) {
                    return 15
                }).iterations(20)
            );
        document.body.append(self.svg.node());
    };

    ForceDirected.prototype.drawGraphs = function () {
        var self = this;
        d3.json("/fdg").then(function (graph) {
            //  f = flatten(graph);
            g = graph;
            self.store = $.extend(true, {}, g);
            restart();
        });

        function restart() {
            ////<editor-fold defaultstate="collapsed" desc="create nodes">

            //add nodes data
            self.node = self.node.data(g.nodes)
            // exit
            self.node.exit().transition().duration(1500)
                .attrTween("cx", function (d) {
                    return function () {
                        return d.x;
                    };
                })
                .attrTween('cy', function (d) {
                    return function () {
                        return d.y;
                    };
                }).remove();
            //exit circles
            self.svg.selectAll("circle").remove();
            self.svg.selectAll("text").remove();
            self.svg.selectAll("title").remove();

            //enter
            var newNode = self.node.enter().append("g").attr("class", "node").on("click", function (d) {

                ////<editor-fold defaultstate="collapsed" desc="circle click function">
                var id = d.id.split('.')[1];
                if (idFilterList.includes(id)) {
                    idFilterList.splice(idFilterList.indexOf(id), 1)
                } else {
                    idFilterList.push(id);
                }
                //console.log(idFilterList);
                filter();
                restart();
                ////</editor-fold>
            }).call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended));
            ;
            //merge

            self.node = self.node.merge(newNode);
            //node.transition().attr("r",0)

            ////</editor-fold>

////<editor-fold defaultstate="collapsed" desc="create links">
            //add data
            self.link = self.link.data(g.links)
            // exit
            self.link.exit().transition().duration(3000)
                .attrTween("x1", function (d) {
                    return function () {
                        return d.source.x;
                    };
                })
                .attrTween("x2", function (d) {
                    return function () {
                        return d.target.x;
                    };
                })
                .attrTween("y1", function (d) {
                    return function () {
                        return d.source.y;
                    };
                })
                .attrTween("y2", function (d) {
                    return function () {
                        return d.target.y;
                    };
                })
                .attr("stroke-opacity", 0).remove();

            //enter
            var rootlink = self.link.enter();


            newLink = rootlink.append("line")
                .attr("class", "link")
                .attr("stroke", function (d) {
                    if (d.type == 2) //household edge
                        return "#00E0D2"
                    else
                        return "#C4CDCE";
                })
                .attr("stroke-opacity", 0.4)
                .attr("stroke-width", function (d) {
                    return Math.sqrt(d.score * 100);
                });

            self.edgepaths = self.svg.selectAll(".edgepath")
                .data(g.links.filter(function (d) {
                    return d.type == 2;
                }))
                .enter().append("path")
                .attr('class', 'edgepath')
                .attr('id', function (d, i) {
                    return 'edgepath' + i
                })
                .attr('d', function (d) {
                })
                .style("pointer-events", "none");
            self.svg.selectAll(".edgepath")
                .data(g.links.filter(function (d) {
                    return d.type == 2;
                })).exit().remove();
            self.edgelabels = self.svg.selectAll(".edgelabel")
                .data(g.links.filter(function (d) {
                    return d.type == 2;
                }));
            self.edgelabels.exit()
            self.edgelabels.enter()
                .append('text')
                .style("pointer-events", "none")
                .attr("class", "edgelabel")
                .attr('id', function (d, i) {
                    return 'edgelabel' + i
                })
                .attr('font-size', 10)
                .attr('fill', '#000000')
                .append('textPath')
                .attr('xlink:href', function (d, i) {
                    return '#edgepath' + i
                })
                .style("pointer-events", "none")
                .text(function (d, i) {
                    return d.score
                });


            //merge
            self.link = self.link.merge(newLink);


////</editor-fold>


////<editor-fold defaultstate="collapsed" desc="create circles">
            idFilterList = [];
            self.circles = self.node.append("circle")
                .attr("r", function (d) {
                    if (d.group == 2)
                        return self.radius * 1.5
                    else return self.radius
                })
                .style("stroke", "#fff")
                .style("stroke-width", "1.5px")
                .attr("fill", function (d) {
                    return self.color(d.group);
                })

////</editor-fold>
////<editor-fold defaultstate="collapsed" desc="create node label and title">

            var lables = self.node.append("text")
                .text(function (d) {
                    return getValue(d.id, '.');
                })
                .style("font-family", "sans-serif")
                .style("font-size", "5px")
                .attr('x', 6)
                .attr('y', 3);

            self.node.append("title")
                .text(function (d) {
                    return d.id;
                });

////</editor-fold>

////<editor-fold defaultstate="collapsed" desc="Add nodes and links to simulation">
            self.simulation
                .nodes(g.nodes)
                .on("tick", ticked);
            self.simulation.force("link")
                .links(g.links);
            self.simulation.alpha(1).alphaTarget(0).restart();

////</editor-fold>
            ////<editor-fold defaultstate="collapsed" desc="testing final nodes and links">

            //a=[]
            // g.links.forEach(function(d, i) {
            //     // id the same, but not key
            //     if (d.source.id.startsWith('785-26-2928') || d.target.id.startsWith('785-26-2928'))
            //         //temp array to hold log data
            //         a.push(d.source.id + "|| " + d.target.id)
            // });

            // newNode.forEach(function(d, i) {
            //     // id the same, but not key
            //    // if (isKey(d))
            //         a.push(d.id)
            // });
            //console.log(newNode)
            ////</editor-fold>

        }

////<editor-fold defaultstate="collapsed" desc="helper functions">


        function getKey(s, del) {
            return s.substring(0, s.indexOf(del))
        }

        function getValue(s, del) {
            return s.substring(s.indexOf(del) + 1)
        }

        function isKey(node) {
            if (getKey(node.id, '.') === getValue(node.id, '.'))
                return true
            else return false
        }


////</editor-fold>
        function collapse(d) {
            if (!d3.event.defaultPrevented) {
                if (d.children) {
                    d._children = d.children;
                    d.children = null;
                } else {
                    d.children = d._children;
                    d._children = null;
                }
                restart();
            }
        }

        function filter() {
            //	add and remove nodes from data based on type filters
            self.store.nodes.forEach(function (n) {

                if (idFilterList.includes(getKey(n.id, '.')) && n.filtered && n.group != 2) {
                    n.filtered = false;
                    g.nodes.push($.extend(true, {}, n));
                } else if (idFilterList.includes(getKey(n.id, '.')) && !n.filtered) {
                    n.filtered = true;

                    g.nodes.forEach(function (d, i) {
                        // id the same, but not key
                        if (n.id === d.id && d.group != "2") {
                            g.nodes.splice(i, 1);

                        }
                    });

                }
            });

            //	add and remove links from data based on availability of nodes
            self.store.links.forEach(function (l) {

                if (l.type == 2) {
                    //TO DO
                    // don't need to do anything as household links are not affected
                }
                if (l.type == 1) {
                    if (idFilterList.includes(l.key) && l.filtered) {
                        l.filtered = false;
                        g.links.push($.extend(true, {}, l))
                    } else if ((idFilterList.includes(l.key) && !l.filtered)) {
                        l.filtered = true;

                        g.links.forEach(function (d, i) {

                            if (l.key === d.key && d.type == 1) {
                                //newarray.push([d.source,d.target])
                                g.links.splice(i, 1);

                            }
                        });
                    }
                }
            });

////<editor-fold defaultstate="collapsed" desc="Debugging for filtering">
            // g.nodes.forEach(function(d, i) {
            //     // id the same, but not key
            //     console.log([getValue(d.id, '.'), getKey(d.id,'.')])
            //     if (getValue(d.id, '.') === getKey(d.id,'.'))
            //         //temp array to hold log data
            //         newarray.push(d.id)
            // });
            // g.links.forEach(function(d, i) {
            //     // id the same, but not key
            //     if (d.source.id.startsWith('785-26-2928') || d.target.id.startsWith('785-26-2928'))
            //         //temp array to hold log data
            //         newarray.push(d)
            // });
////</editor-fold>
        }

        // Returns a list of all nodes under the root.
        // function flatten(root) {
        //     var nodes = [], i = 0;
        //
        //     function recurse(node) {
        //         if (node.children) node.children.forEach(recurse);
        //         if (!node.id) node.id = ++i;
        //         nodes.push(node);
        //     }
        //
        //     recurse(root);
        //     return nodes;
        // }
        function ticked() {
            self.link
                .attr("x1", function (d) {
                    return d.source.x;
                })
                .attr("y1", function (d) {
                    return d.source.y;
                })
                .attr("x2", function (d) {
                    return d.target.x;
                })
                .attr("y2", function (d) {
                    return d.target.y;
                });

            self.edgepaths
                .attr("d", function (d) {
                    return 'M ' + d.source.x + ' ' + d.source.y + ' L ' + d.target.x + ' ' + d.target.y
                });


            self.node
                .attr("transform", function (d) {
                    return "translate(" + d.x + "," + d.y + ")";
                })

            self.edgelabels
                .attr('transform', function (d, i) {
                    if (d.target.x < d.source.x) {
                        bbox = this.getBBox();
                        rx = bbox.x + bbox.width / 2;
                        ry = bbox.y + bbox.height / 2;
                        return 'rotate(180 ' + rx + ' ' + ry + ')';
                    } else {
                        return 'rotate(0)';
                    }
                });
        }

        function dragstarted(d) {
            if (!d3.event.active) self.simulation.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(d) {
            d.fx = d3.event.x;
            d.fy = d3.event.y;
        }

        function dragended(d) {
            if (!d3.event.active) self.simulation.alphaTarget(0);
            d.fx = null;
            d.fy = null;
        }

        function deleteNode(g) {
            self.exiting = self.node.data(g.nodes).exit().transition().duration(1500)
                .attr("r", 0)
                .remove();

            var linkdata = self.link.data(g.links);
            // Keep the exiting links connected to the moving remaining nodes.
            linkdata.exit().transition().duration(1500)
                .attr("stroke-opacity", 0)
                .attrTween("x1", function (d) {
                    return function () {
                        return d.source.x;
                    };
                })
                .attrTween("x2", function (d) {
                    return function () {
                        return d.target.x;
                    };
                })
                .attrTween("y1", function (d) {
                    return function () {
                        return d.source.y;
                    };
                })
                .attrTween("y2", function (d) {
                    return function () {
                        return d.target.y;
                    };
                })
                .remove();
            self.simulation
                .nodes(g.nodes)
                .on("tick", ticked);
            self.simulation.force("link")
                .links(g.links);
            self.simulation.alpha(1).alphaTarget(0).restart();
            // self.simulation
            //     .nodes(g.nodes)
            //     .on("tick", ticked);
            //
            // self.simulation.force("link")
            //     .links(g.links).start();
        }

        //Creating the button
        d3.select("body")
            .append("div")
            .attr("class", "fdg-button")

        var findHousehold = d3.select(".fdg-button")
            .append("input")
            .attr("type", "button")
            .attr("value", "Derive HouseHold");

        findHousehold.on("click", function () {
            self.circles
                .transition().duration(750).attr("r", 5);
            $.ajax(
                {
                    url: '/fdg?type=household',
                    type: 'get',
                    dataType: "json",
                    success: successFunc,
                    error: errorFunc
                });

            function successFunc(data) {
                g.nodes = data.nodes;
                g.links = data.links;
                restart();
            }

            function errorFunc() {
                alert('MVC controller call failed.');
            }


        });
        var detachHousehold = d3.select(".fdg-button")
            .append("input")
            .attr("type", "button")
            .attr("value", "Return")
            .on("click", function () {
                self.circles
                    .transition().duration(750).attr("r", 5);
                d3.json("/fdg").then(function (graph2) {

                    g.nodes = graph2.nodes;
                    g.links = graph2.links;
                    restart();
                });
            });
        var cancel = d3.select(".fdg-button")
            .append("input")
            .attr("type", "button")
            .attr("value", "Cancel")
            .on("click", function () {
                $("#header").animate({
                    opacity: 1,
                    height: "toggle"
                }, 3000, function () {
                });
                $("svg").animate({
                    height: "toggle"
                }, 3000, function () {
                    $("svg").remove();
                });
                $(".fdg-button").animate({
                    height: "toggle"
                }, 3000, function () {
                    $(".fdg-button").remove();
                });
            })

    }
    return ForceDirected;
})();


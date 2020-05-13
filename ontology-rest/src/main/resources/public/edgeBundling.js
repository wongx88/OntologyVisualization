var EdgeBundling = (function () {


    function EdgeBundling(data, elem, width = 1200, height = 610, radius = 477) {
        this.width = width;
        this.height = height;
        this.data = data;
        this.radius = radius;
        this.colornone = "#ccc";
        this.colorout = "#f00";
        this.colorin = "#00f";
        this.color = "rgb(63,255,80)";
        this.init();
        this.drawEdgeBundling();
    }

    EdgeBundling.prototype.init = function () {
        var self = this;
        var searchbox = $('.button-set').prepend($("    <form id='search' class='field'>\n" +
            "    <input type=\"text\" maxlength=\"11\" name=\"SSN\"/>\n" +
            "<label for=\"register\"><span>SSN</span></label>" +
            "        <input type=\"submit\" id=\"submitButton\" value=\"submit\"/>\n" +
            "        </form><p></p>\n")).hide();
        searchbox.fadeToggle(1000);

        var $form = $('.field');

        function validateSSN(ssn) {
            var regex = /^\d{3}-\d{2}-\d{3}$/;
            return regex.test(ssn);
        };

        $form.hover(
            function () {
                $(this).fadeTo("slow", 1);
            },
            function () {
                $(this).fadeTo("slow", 0.2);
            }
        );
        $('input[type="text"]').on('keyup', function (e) {
            var $this = $(this),
                $input = $this.val();
            if ($input.length > 0) {
                $form.find('label').addClass('active');
                if (validateSSN($input)) {
                    $form.find('input[type="submit"]').addClass('active');
                    if (e.which === 13) {
                        $form.find('input[type="submit"]').click();
                        $this.blur();
                    }
                } else {
                    $form.find('input[type="submit"]').removeClass('active');
                }
                $(this).addClass('active');
            } else {
                $form.find('label').removeClass('active');
                $form.find('input[type="submit"]').removeClass('active');
                $(this).removeClass('active');
            }
        });

        $form.on('click', 'input[type="submit"].active', function (e) {
            e.preventDefault;
            var $this = $(this);
            $(this).addClass('full');

            setTimeout(() => {
                $form.find('input[type="text"]').val('').removeClass('active');
                $form.find('label').removeClass('active');
                $this.removeClass('full active');
            }, 1200);
        })

        //975 610
        self.svg = d3.create("svg")
            //.attr("viewBox", [-self.width / 2, -self.width / 2, self.width, self.width]);
        .attr("viewBox", [-self.width , -self.width / 2, self.width * 2, self.width]);

        this.tree = d3.cluster()
            .size([2 * Math.PI, self.radius - 100]);

        this.line = d3.lineRadial()
            .curve(d3.curveBundle.beta(0.85))
            .radius(d => d.y)
            .angle(d => d.x);
        this.selectedThis = d3.select(self);
    };


    EdgeBundling.prototype.drawEdgeBundling = function () {

        var self = this;
        $('form#search').submit(function (e) {
            e.preventDefault();
            var values = $(this).serialize();
            $.ajax({
                url: 'getRecordsBySSN',
                dataType: "json",
                data: values,
                type: 'get',
                success: function (data) {
                    self.svg.selectAll("g").remove();
                    const selectThis = self.selectedThis

                    function id(node) {
                        return `${node.parent ? id(node.parent) + "." : ""}${node.data.name}`;
                    }

                    function bilink(root) {
                        const map = new Map(root.leaves().map(d => [id(d), d]));
                        for (const d of root.leaves()) d.incoming = [], d.outgoing = d.data.relatesToObjs.map(i => [d, map.get(i)]);
                        for (const d of root.leaves()) for (const o of d.outgoing) o[1].incoming.push(o);
                        return root;
                    }

                    function hierarchy(data, delimiter = ".") {
                        let root;
                        const map = new Map;

                        data.forEach(function find(data) {
                            const {name} = data;
                            // console.log("name" , name);
                            if (map.has(name)) return map.get(name);
                            const i = name.indexOf(delimiter);
                            map.set(name, data);
                            if (i >= 0) {
                                //console.log("name substring i + 1 ",  name.substring(i + 1));
                                find({name: name.substring(0, i), children: []}).children.push(data);
                                data.name = name.substring(i + 1);
                            } else {
                                root = data;
                            }
                            return data;
                        });
                        return root;
                    }

                    data = hierarchy(data);
                    const root = self.tree(bilink(d3.hierarchy(data)
                        .sort((a, b) => d3.ascending(a.height, b.height) || d3.ascending(a.data.name, b.data.name))));


                    const node = self.svg.append("g")
                        .attr("font-family", "-apple-system, BlinkMacSystemFont, sans-serif")
                        .attr("font-size", 20)
                        .selectAll("g")
                        .data(root.leaves())
                        .join("g")
                        .attr("transform", d => `rotate(${d.x * 180 / Math.PI - 90}) translate(${d.y},0)`)
                        .append("text") //append text tag with below attrs
                        .attr("dy", "0.31em")
                        .attr("x", d => d.x < Math.PI ? 6 : -6)
                        .attr("text-anchor", d => d.x < Math.PI ? "start" : "end")
                        .attr("transform", d => d.x >= Math.PI ? "rotate(180)" : null)
                        .text(d => d.data.name) // set text to name
                        .attr("fill", self.color)
                        .each(function (d) {
                            d.text = this;
                        })
                        .on("mouseover", function (d) {
                            //    console.log(link)
                            link.style("mix-blend-mode", null);
                            d3.select(this).attr("font-weight", "bold");
                            d3.selectAll(d.incoming.map(d => d.path)).attr("stroke", self.colorin).raise();
                            d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", self.colorin).attr("font-weight", "bold");
                            d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", self.colorout).raise();
                            d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", self.colorout).attr("font-weight", "bold");
                        })
                        .on("mouseout", function (d) {
                            link.style("mix-blend-mode", "multiply");
                            d3.select(this).attr("font-weight", null);
                            d3.selectAll(d.incoming.map(d => d.path)).attr("stroke", null);
                            d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", self.color).attr("font-weight", null);
                            d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", null);
                            d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", self.color).attr("font-weight", null);
                        })
                        .call(text => text.append("title").text(d => `${id(d)} ${d.outgoing.length} outgoing ${d.incoming.length} incoming`));
                    const link = self.svg.append("g")
                        .attr("stroke", self.color)
                        .attr("fill", "none")
                        .selectAll("path")
                        .data(root.leaves().flatMap(leaf => leaf.outgoing))
                        .join("path")
                        .style("mix-blend-mode", "multiply")
                        .attr("d", ([i, o]) => self.line(i.path(o)))
                        .each(function (d) {
                            d.path = this;
                        });

                    document.body.prepend(self.svg.node());
                    $("svg").hide();
                    $("svg").fadeToggle(1000);
                }
            });

        });
        // d3.json("/heb").then(function (data) {
        //     const selectThis = self.selectedThis
        //
        //     function overed(d) {
        //         link.style("mix-blend-mode", null);
        //         selectThis[0].attr("font-weight", "bold");
        //         d3.selectAll(d.incoming.map(d => d.path)).attr("stroke", self.colorin).raise();
        //         d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", self.colorin).attr("font-weight", "bold");
        //         d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", self.colorout).raise();
        //         d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", self.colorout).attr("font-weight", "bold");
        //     }
        //
        //     function outed(d) {
        //         link.style("mix-blend-mode", "multiply");
        //         selectThis[0].attr("font-weight", null);
        //         d3.selectAll(d.incoming.map(d => d.path)).attr("stroke", null);
        //         d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", null).attr("font-weight", null);
        //         d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", null);
        //         d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", null).attr("font-weight", null);
        //     }
        //
        //     function id(node) {
        //         return `${node.parent ? id(node.parent) + "." : ""}${node.data.name}`;
        //     }
        //
        //     function bilink(root) {
        //         const map = new Map(root.leaves().map(d => [id(d), d]));
        //         for (const d of root.leaves()) d.incoming = [], d.outgoing = d.data.relatesToObjs.map(i => [d, map.get(i)]);
        //         for (const d of root.leaves()) for (const o of d.outgoing) o[1].incoming.push(o);
        //         return root;
        //     }
        //
        //     function hierarchy(data, delimiter = ".") {
        //         let root;
        //         const map = new Map;
        //
        //         data.forEach(function find(data) {
        //             const {name} = data;
        //             // console.log("name" , name);
        //             if (map.has(name)) return map.get(name);
        //             const i = name.indexOf(delimiter);
        //             map.set(name, data);
        //             if (i >= 0) {
        //                 //console.log("name substring i + 1 ",  name.substring(i + 1));
        //                 find({name: name.substring(0, i), children: []}).children.push(data);
        //                 data.name = name.substring(i + 1);
        //             } else {
        //                 root = data;
        //             }
        //             return data;
        //         });
        //         return root;
        //     }
        //
        //     data = hierarchy(data);
        //     const root = self.tree(bilink(d3.hierarchy(data)
        //         .sort((a, b) => d3.ascending(a.height, b.height) || d3.ascending(a.data.name, b.data.name))));
        //
        //
        //     const node = self.svg.append("g")
        //         .attr("font-family", "sans-serif")
        //         .attr("font-size", 10)
        //         .selectAll("g")
        //         .data(root.leaves())
        //         .join("g")
        //         .attr("transform", d => `rotate(${d.x * 180 / Math.PI - 90}) translate(${d.y},0)`)
        //         .append("text") //append text tag with below attrs
        //         .attr("dy", "0.31em")
        //         .attr("x", d => d.x < Math.PI ? 6 : -6)
        //         .attr("text-anchor", d => d.x < Math.PI ? "start" : "end")
        //         .attr("transform", d => d.x >= Math.PI ? "rotate(180)" : null)
        //         .text(d => d.data.name) // set text to name
        //         .each(function (d) {
        //             d.text = this;
        //         })
        //         .on("mouseover", function (d) {
        //             //    console.log(link)
        //             link.style("mix-blend-mode", null);
        //             d3.select(this).attr("font-weight", "bold");
        //             d3.selectAll(d.incoming.map(d => d.path)).attr("stroke", self.colorin).raise();
        //             d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", self.colorin).attr("font-weight", "bold");
        //             d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", self.colorout).raise();
        //             d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", self.colorout).attr("font-weight", "bold");
        //         })
        //         .on("mouseout", function (d) {
        //             link.style("mix-blend-mode", "multiply");
        //             d3.select(this).attr("font-weight", null);
        //             d3.selectAll(d.incoming.map(d => d.path)).attr("stroke", null);
        //             d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", null).attr("font-weight", null);
        //             d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", null);
        //             d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", null).attr("font-weight", null);
        //         })
        //         .call(text => text.append("title").text(d => `${id(d)} ${d.outgoing.length} outgoing ${d.incoming.length} incoming`));
        //     const link = self.svg.append("g")
        //         .attr("stroke", self.colornone)
        //         .attr("fill", "none")
        //         .selectAll("path")
        //         .data(root.leaves().flatMap(leaf => leaf.outgoing))
        //         .join("path")
        //         .style("mix-blend-mode", "multiply")
        //         .attr("d", ([i, o]) => self.line(i.path(o)))
        //         .each(function (d) {
        //             d.path = this;
        //         });
        //
        //     document.body.append(self.svg.node());
        // });
    }
    return EdgeBundling;
})();


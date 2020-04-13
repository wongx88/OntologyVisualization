var EdgeBundling = (function () {

    function EdgeBundling(data, elem, width = 975, height = 610, radius = 477) {
        this.width = width;
        this.height = height;
        this.data = data;
        this.radius = radius;
        this.colornone = "#ccc";
        this.colorout = "#f00";
        this.colorin = "#00f";
        this.init();
        this.drawEdgeBundling();
    }

    EdgeBundling.prototype.init = function () {
        var self = this;
        //975 610
        self.svg = d3.create("svg")
            .attr("viewBox", [-self.width / 2, -self.width / 2, self.width, self.width]);

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

        d3.json("/heb").then(function (data) {
            const selectThis = self.selectedThis

            function overed(d) {
                link.style("mix-blend-mode", null);
                selectThis[0].attr("font-weight", "bold");
                d3.selectAll(d.incoming.map(d => d.path)).attr("stroke", self.colorin).raise();
                d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", self.colorin).attr("font-weight", "bold");
                d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", self.colorout).raise();
                d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", self.colorout).attr("font-weight", "bold");
            }

            function outed(d) {
                link.style("mix-blend-mode", "multiply");
                selectThis[0].attr("font-weight", null);
                d3.selectAll(d.incoming.map(d => d.path)).attr("stroke", null);
                d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", null).attr("font-weight", null);
                d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", null);
                d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", null).attr("font-weight", null);
            }

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
                .attr("font-family", "sans-serif")
                .attr("font-size", 10)
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
                    d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", null).attr("font-weight", null);
                    d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", null);
                    d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", null).attr("font-weight", null);
                })
                .call(text => text.append("title").text(d => `${id(d)} ${d.outgoing.length} outgoing ${d.incoming.length} incoming`));
            const link = self.svg.append("g")
                .attr("stroke", self.colornone)
                .attr("fill", "none")
                .selectAll("path")
                .data(root.leaves().flatMap(leaf => leaf.outgoing))
                .join("path")
                .style("mix-blend-mode", "multiply")
                .attr("d", ([i, o]) => self.line(i.path(o)))
                .each(function (d) {
                    d.path = this;
                });

            document.body.append(self.svg.node());
        });
    }
    return EdgeBundling;
})();


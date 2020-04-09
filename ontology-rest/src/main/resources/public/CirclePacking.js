var CirclePacking = (function () {
    function CirclePacking(width = 932, height = 932, radius = 10) {
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.init();
        this.drawGraphs();
    }

    CirclePacking.prototype.init = function () {
        var self = this;
        //975 610
        self.color = d3.scaleOrdinal(d3.schemeCategory10);
        self.format = d3.format(",d")
        self.simulation = d3.forceSimulation()
            .force("link", d3.forceLink().id(function (d) {
                return d.id;
            }))//.distance([500]))
            .force("charge", d3.forceManyBody())
            .force("center", d3.forceCenter(this.width / 2, this.height / 2))
            .force('collide', d3.forceCollide(function (d) {
                return d.id === "j" ? 100 : 30
            }));

        this.pack = data => d3.pack()
            .padding(3)
            .size([self.width, self.height])

            (d3.hierarchy(data)
                .count()
                .sort((a, b) => b.value - a.value))
        // console.log(this.pack)
        //


    };

    CirclePacking.prototype.drawGraphs = function () {
        var self = this;

        d3.json("/circlePacking").then(function (data) {
            console.log(d3.hierarchy(data)
                .sum(d => d.value)
                .sort((a, b) => b.value - a.value))
            this.root = self.pack(data);
            console.log(this.root)
            let focus = root;
            let view;
            const svg = d3.create("svg")
                .attr("viewBox", `-${self.width / 2} -${self.height / 2} ${self.width} ${self.height}`)
                .style("display", "block")
                .style("margin", "0 -14px")
                .style("background", self.color(0))
                .style("cursor", "pointer")
                .on("click", () => zoom(root));
            const node = svg.append("g")
                .selectAll("circle")
                .data(root.descendants().slice(1))
                .join("circle")
                .attr("fill", d => d.children ? self.color(d.depth) : "white")
                .attr("pointer-events", d => !d.children ? "none" : null)
                .on("mouseover", function () {
                    d3.select(this).attr("stroke", "#000");
                })
                .on("mouseout", function () {
                    d3.select(this).attr("stroke", null);
                })
                .on("click", d => focus !== d && (zoom(d), d3.event.stopPropagation()));

            function zoom(d) {
                const focus0 = focus;

                focus = d;

                const transition = svg.transition()
                    .duration(d3.event.altKey ? 7500 : 750)
                    .tween("zoom", d => {
                        const i = d3.interpolateZoom(view, [focus.x, focus.y, focus.r * 2]);
                        return t => zoomTo(i(t));
                    });

                label
                    .filter(function (d) {
                        return d.parent === focus || this.style.display === "inline";
                    })
                    .transition(transition)
                    .style("fill-opacity", d => d.parent === focus ? 1 : 0)
                    .on("start", function (d) {
                        if (d.parent === focus) this.style.display = "inline";
                    })
                    .on("end", function (d) {
                        if (d.parent !== focus) this.style.display = "none";
                    });
            }

            const label = svg.append("g")
                .style("font", "10px sans-serif")
                .attr("pointer-events", "none")
                .attr("text-anchor", "middle")
                .selectAll("text")
                .data(root.descendants())
                .join("text")
                .style("fill-opacity", d => d.parent === root ? 1 : 0)
                .style("display", d => d.parent === root ? "inline" : "none")
                .text(d => d.data.name);


            zoomTo([root.x, root.y, root.r * 2]);

            function zoomTo(v) {
                const k = self.width / v[2];

                view = v;

                label.attr("transform", d => `translate(${(d.x - v[0]) * k},${(d.y - v[1]) * k})`);
                node.attr("transform", d => `translate(${(d.x - v[0]) * k},${(d.y - v[1]) * k})`);
                node.attr("r", d => d.r * k);
            }


            document.body.append(svg.node());
        })
    }
    return CirclePacking;
})();

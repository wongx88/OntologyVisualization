var BubbleMap_3 = (function () {
    function BubbleMap(data, width = 975, height = 610, minRadius, maxRadius) {
        this.width = width;
        this.height = height;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.init();
        this.drawCountries();
    }

    BubbleMap.prototype.init = function () {
        var self = this;
        self.svg = d3.create("svg")
            //975 610
            .attr("viewBox", [0, 0, this.width, this.height])
            .attr("class", 'BubbleMap');
        self.path = d3.geoPath();
        self.format = d3.format(",.0f");

    };

    BubbleMap.prototype.drawCountries = async function () {
        var self = this;
        await d3.json("us-map.json").then(function (data) {
            self.topology = data;
        });

        self.svg.append("path")
            .datum(topojson.feature(self.topology, self.topology.objects.nation))
            .attr("fill", "#daffdb")
            .attr("d", self.path);

        self.svg.append("path")
            .datum(topojson.mesh(self.topology, self.topology.objects.states, (a, b) => a !== b))
            .attr("fill", "none")
            .attr("stroke", "white")
            .attr("stroke-linejoin", "round")
            .attr("d", self.path);

        const legend = self.svg.append("g")
            .attr("fill", "#a9af60")
            .attr("transform", "translate(925,608)")
            .attr("text-anchor", "middle")
            .style("font", "10px sans-serif")
            .selectAll("g")
            .data([1, 3, 6, 9, 12, 15])  //controls the scale circles
            .join("g");

        // self.data = new Map((await d3.json("https://gist.githubusercontent.com/mbostock/5ff33e1f3a3f9d6b1b38c8a79df86377/raw/0d71e5d21c9e44fed63b41c2e8b2f28ffd133213/population.json")).slice(1).map(([population, state, county]) => [state + county, +population]));

        data = await d3.csv("/customers").then(function (data) {

            data = d3.pairs(data, ({'Billing_State': address}, {'Transaction Reference Number': txn}) => {
                return {state: address.trim(), transaction: +txn};
            })
            var nested_data = d3.nest()
                .key(function (d) {
                    return d.state;
                })
                .key(function (d) {
                    return d.transaction;
                })
                .rollup(function (txns) {
                    return 1;
                }).entries(data).map(function (d) {
                    var ob = {};
                    ob.state = d.key;
                    ob.amount = d.values.length;
                    return ob;
                });

            self.data = d3.rollup(nested_data, v => d3.sum(v, d => +d.amount), d => d.state)

            self.radius = d3.scaleSqrt([0, d3.quantile([...self.data.values()].sort(d3.ascending), 0.485)], [this.minRadius = 0, this.maxRadius = 15])

            legend.append("circle")
                .attr("fill", "none")
                .attr("stroke", "#ccc")
                .attr("cy", d => -self.radius(d))
                .attr("r", self.radius);

            legend.append("text")
                .attr("y", d => -2 * self.radius(d))
                .attr("dy", "1.3em")
                .text(d3.format(".1s"));

            // console.log(topojson.feature(self.topology, self.topology.objects.counties).features.map(d => (d.value = d.properties.name,d)))
            self.svg.append("g")
                .attr("fill", "black")
                .attr("fill-opacity", 0.5)
                .attr("stroke", "#fff")
                .attr("stroke-width", 0.5)
                .selectAll("circle")
                .data(
                    topojson.feature(self.topology, self.topology.objects.states).features
                        .map(d => (d.value = self.data.get(d.properties.name), d))
                        .sort((a, b) => b.value - a.value)
                )
                .join("circle")
                .attr("transform", d => `translate(${self.path.centroid(d)})`)
                .attr("r", d => self.radius(d.value))
                .append("title")
                .text(d => `${d.properties.name};${self.format(d.value)}`);


            document.body.append(self.svg.node());
            return self.svg.node();

        })


    };
    return BubbleMap;
})();


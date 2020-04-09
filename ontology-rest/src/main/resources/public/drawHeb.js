var draw = function () {
    data = {}
    // data = d3.json("us-map.json").then(function(data){
    //     self.topology = data;
    // });
    //new BubbleMap(data);
    new EdgeBundling(data, width = 3000, height = 950);
    // new ForceDirected();
};

window.onload = draw;
// var draw = function () {
//     data = {}
//     // data = d3.json("us-map.json").then(function(data){
//     //     self.topology = data;
//     // });
//     //new BubbleMap_3(data, 2000, 1000);
//      new EdgeBundling(data);
//     //new ForceDirected();
//     //new CirclePacking();
//     //
// };
//
var renderPage = function (type) {

    if (type == "ForceDirected")
        new ForceDirected(width = 1200, height = 950);
    else if (type == "EdgeBundling")
        new EdgeBundling();
    else if (type == "HeatMap#1")
        new BubbleMap();
    else if (type == "HeatMap#2")
        new BubbleMap_2();
    else if (type == "HeatMap#3")
        new BubbleMap_3();
    else
        new CirclePacking();
};


// window.onload = draw;
$(document).ready(function () {
    $("input[id='visual-1']").click(function () {
        var radioValue = $("input[name='vis']:checked").val();
        if (radioValue) {
            $("#header").animate({
                opacity: 0.25,
                height: "toggle"
            }, 3000, function () {
                // Animation complete.
            });
            renderPage(radioValue)
        }
    });
});

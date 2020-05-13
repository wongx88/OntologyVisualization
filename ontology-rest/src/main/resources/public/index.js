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
        new ForceDirected(width = 575, height = 225);
    else if (type == "EdgeBundling") {
        new EdgeBundling();
    } else if (type == "HeatMap#1") {
        new BubbleMap();
        $(".title").append($("<h1>Transaction Amount</h1>"));
    } else if (type == "HeatMap#2") {
        new BubbleMap_2();
        $(".title").append($("<h1>Average Household Size</h1>"));
    } else if (type == "HeatMap#3") {
        new BubbleMap_3();
        $(".title").append($("<h1>Transaction Volume</h1>"));
    } else
        new CirclePacking();
};


// window.onload = draw;
$(document).ready(function () {
    d3.select("body")
        .append("div")
        .attr("class", "title")
    $("input[id='visual-1']").click(function (e) {
        e.preventDefault();
        $("#visual-1").attr("disabled", true);
        var radioValue = $("input[name='vis']:checked").val();
        if (radioValue) {
            $("#header").fadeToggle(1000, function () {
                d3.select("body")
                    .append("div")
                    .attr("class", "button-set");
                var cancel = d3.select(".button-set")
                    .append("input")
                    .attr("type", "button")
                    .attr("value", "Cancel")
                    .on("click", function () {
                        $("#visual-1").attr("disabled", false);
                        $("#header").fadeToggle({
                            opacity: 1,
                            height: "toggle"
                        }, 1000, function () {
                        });
                        $("svg").animate({
                            height: "toggle"
                        }, 1000, function () {
                            $("svg").remove();
                        });
                        $(".button-set").fadeToggle(1000, function () {
                            $(".button-set").remove();
                        });
                        $('.field').fadeToggle(1000, function () {
                            $(".field").remove();
                        });
                        $('h1').fadeToggle(1000, function () {
                            $("h1").remove();
                        });

                    })
                // Animation complete.
                renderPage(radioValue);
            });

        }
    });
});

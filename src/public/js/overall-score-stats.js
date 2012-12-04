$(function () {
    var graphElem = $('#current-overall-scores');

    if (graphElem[0]) {
        var url = graphElem.attr('data-url');

        var drawBarGraph = function (data) {
            var mapDataSeries = function (data) {
                return data.map(function (x, index) {
                    return {
                        label:x.name,
                        data:[
                            [index, x.score]
                        ]
                    }
                });
            };

            var mapDataNames = function (data) {
                return data.map(function (x, index) {
                    return [index, x.name];
                });
            };

            $.plot(graphElem, mapDataSeries(data), {
                series:{
                    lines:{show:false, steps:false},
                    bars:{show:true, barWidth:0.9, align:'center'}
                },
                xaxis:{
                    ticks:mapDataNames(data)
                }
            });
        };

        var lastData = {};

        var updateStats = function () {
            $.getJSON(url, function (data) {
                lastData = data;
                drawBarGraph(lastData);
            });
        };

        setInterval(updateStats, 5000);
        updateStats();

        $(window).resize(function () {
            drawBarGraph(lastData);
        });
    }
});

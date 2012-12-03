$(function () {

    function plotScoreHistory(element, data) {
        $.plot(element, [ data ], {
            series: {
                lines: { show: true, fill: true, steps: false },
                stack: true
            },
            xaxis: {
                ticks: 3,
                tickFormatter: function (val, axis) {
                    return $.format.date(new Date(val), "HH:mm");
                }
            },
            yaxis: { min: 0, minTickSize: 1, tickDecimals: 0}
        });
    }

    $(".score-history").each(function (index, element) {
        plotScoreHistory(element, []);
        var url = $(element).attr("data-url");
        $.ajax({
            url: url,
            success: function (data) {
                plotScoreHistory(element, data);
            }
        });
    });
});

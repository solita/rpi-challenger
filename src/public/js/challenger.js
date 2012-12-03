$(function () {

    // Use the same scale for all participants to make comparing them easier
    var tournamentStart = Number.MAX_VALUE;
    var tournamentMaxPoints = 3;

    function plotScoreHistory(element) {
        var data = element.scoreHistory || [];
        $.plot(element, [ data ], {
            series: {
                lines: { show: true, fill: true, steps: false },
                stack: true
            },
            xaxis: {
                min: tournamentStart,
                ticks: 5,
                tickFormatter: function (val, axis) {
                    return $.format.date(new Date(val), "HH:mm");
                },
                font: { size: 9 }

            },
            yaxis: {
                min: 0,
                max: tournamentMaxPoints,
                minTickSize: 1,
                tickDecimals: 0
            }
        });
    }

    function plotAllScoreHistories() {
        $(".score-history").each(function (index, element) {
            plotScoreHistory(element);
        });
    }

    plotAllScoreHistories();

    $(".score-history").each(function (index, element) {
        $.ajax({
            url: $(element).attr("data-url"),
            success: function (data) {
                element.scoreHistory = data;
                tournamentStart = Math.min(tournamentStart, data[0] ? data[0][0] : Number.MAX_VALUE);
                tournamentMaxPoints = Math.max(tournamentMaxPoints, data.reduce(function (maxPoints, round) {
                    return Math.max(maxPoints, round[1]);
                }, 0));
                plotAllScoreHistories();
            }
        });
    });
});

$(function () {

    // Use the same scale for all participants to make comparing them easier
    var tournamentStart = Number.MAX_VALUE;
    var tournamentMaxPoints = 3;

    function plotScoreHistory(element) {
        var scoreHistory = element.scoreHistory || [];
        var deficitHistory = element.deficitHistory || [];
        $.plot(element, [ scoreHistory, deficitHistory ], {
            series: {
                lines: {
                    lineWidth: 1,
                    fill: true,
                    fillColor: {
                        colors: [
                            { opacity: 0.8 },
                            { opacity: 0.3 }
                        ]
                    }
                },
                stack: true
            },
            colors: [ "#F59C00", "#FF07D4" ],
            xaxis: {
                min: tournamentStart,
                tickSize: 30 * 60 * 1000,
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
                element.scoreHistory = data.map(function (val) {
                    return [val[0], val[1]];
                });
                element.deficitHistory = data.map(function (val) {
                    return [val[0], val[2] - val[1]];
                });

                tournamentStart = Math.min(tournamentStart, data[0] ? data[0][0] : Number.MAX_VALUE);
                tournamentMaxPoints = Math.max(tournamentMaxPoints, data.reduce(function (maxPoints, round) {
                    return Math.max(maxPoints, round[1]);
                }, 0));
                plotAllScoreHistories();
            }
        });
    });
});

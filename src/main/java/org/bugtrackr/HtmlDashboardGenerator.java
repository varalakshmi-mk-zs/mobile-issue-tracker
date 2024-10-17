package org.bugtrackr;

import org.kohsuke.github.GHIssue;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class HtmlDashboardGenerator {

    public void generateHtmlFile(
            List<GHIssue> androidLast7Days, List<GHIssue> androidLastMonth, List<GHIssue> androidMoreThanOneMonth, String androidTitle,
            List<GHIssue> iosLast7Days, List<GHIssue> iosLastMonth, List<GHIssue> iosMoreThanOneMonth, String iosTitle) throws IOException {

        String androidTooltipLast7Days = buildTooltipData(androidLast7Days, "Android", "Last 7 Days");
        String androidTooltipLastMonth = buildTooltipData(androidLastMonth, "Android", "Last 1 Month");
        String androidTooltipMoreThanOneMonth = buildTooltipData(androidMoreThanOneMonth, "Android", "More Than 1 Month");

        String iosTooltipLast7Days = buildTooltipData(iosLast7Days, "iOS", "Last 7 Days");
        String iosTooltipLastMonth = buildTooltipData(iosLastMonth, "iOS", "Last 1 Month");
        String iosTooltipMoreThanOneMonth = buildTooltipData(iosMoreThanOneMonth, "iOS", "More Than 1 Month");

        String htmlContent = String.format("""
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Issues Dashboard</title>
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <style>
            body {
                display: flex;
                flex-direction: column;
                align-items: center;
                height: 100vh;
                margin: 0;
                font-family: Arial, sans-serif;
            }
            h1 {
                margin-bottom: 20px;
            }
            .chart-container {
                width: 300px;
                height: 300px;
                position: relative;
                margin: 20px;
            }
            .charts-wrapper {
                display: flex;
                justify-content: center;
                flex-wrap: wrap;
            }
            .custom-tooltip {
                display: none;
                position: absolute;
                background-color: rgba(0, 0, 0, 0.8);
                color: white;
                padding: 10px;
                border-radius: 5px;
                max-width: 300px;
                z-index: 10;
                pointer-events: none;
            }
            .total-count {
                text-align: center;
                margin-top: 10px;
                font-weight: bold;
            }
            .color-box {
                display: inline-block;
                width: 15px;
                height: 15px;
                margin-right: 5px;
                vertical-align: middle;
            }
        </style>
    </head>
    <body>
        <h1>Bug Tracking Dashboard</h1>
        <div class="charts-wrapper">
            <div class="chart-container">
                <h2>%s</h2>
                <canvas id="androidChart"></canvas>
                <div id="android-tooltip" class="custom-tooltip"></div>
                <div class="total-count">
                    <div id="android-count-last7" class="count-label"></div>
                    <div id="android-count-last1month" class="count-label"></div>
                    <div id="android-count-morethan1month" class="count-label"></div>
                </div>
            </div>
            <div class="chart-container">
                <h2>%s</h2>
                <canvas id="iosChart"></canvas>
                <div id="ios-tooltip" class="custom-tooltip"></div>
                <div class="total-count">
                    <div id="ios-count-last7" class="count-label"></div>
                    <div id="ios-count-last1month" class="count-label"></div>
                    <div id="ios-count-morethan1month" class="count-label"></div>
                </div>
            </div>
        </div>
    
        <script>
            const androidCtx = document.getElementById('androidChart').getContext('2d');
            const iosCtx = document.getElementById('iosChart').getContext('2d');

            const androidData = {
                labels: ['Last 7 Days', 'Last 1 Month', 'More Than 1 Month'],
                datasets: [{
                    label: '# of Issues',
                    data: [%d, %d, %d],
                    backgroundColor: ['rgba(255, 99, 132, 0.8)', 'rgba(54, 162, 235, 0.8)', 'rgba(255, 206, 86, 0.8)'],
                    borderColor: ['rgba(255, 99, 132, 1)', 'rgba(54, 162, 235, 1)', 'rgba(255, 206, 86, 1)'],
                    borderWidth: 1
                }]
            };

            const iosData = {
                labels: ['Last 7 Days', 'Last 1 Month', 'More Than 1 Month'],
                datasets: [{
                    label: '# of Issues',
                    data: [%d, %d, %d],
                    backgroundColor: ['rgba(255, 99, 132, 0.8)', 'rgba(54, 162, 235, 0.8)', 'rgba(255, 206, 86, 0.8)'],
                    borderColor: ['rgba(255, 99, 132, 1)', 'rgba(54, 162, 235, 1)', 'rgba(255, 206, 86, 1)'],
                    borderWidth: 1
                }]
            };

            function createChart(ctx, data, tooltipData, tooltipElement, countLabels) {
                const chart = new Chart(ctx, {
                    type: 'doughnut',
                    data: data,
                    options: {
                        responsive: true,
                        maintainAspectRatio: true,
                        plugins: {
                            tooltip: {
                                enabled: false // Disable default tooltip
                            },
                        },
                        onHover: function(event) {
                            const points = chart.getElementsAtEventForMode(event, 'nearest', { intersect: true }, false);
                            const tooltip = document.getElementById(tooltipElement);
                            if (points.length) {
                                const index = points[0].index;
                                const issues = tooltipData[index];
                                tooltip.innerHTML = '<strong>Total Issues:</strong> ' + issues.total +
                                    '<br><br>' + issues.details.map(issue =>
                                    'ID: ' + issue.id + '<br>Title: ' + issue.title +
                                    '<br>Created At: ' + issue.createdAt +
                                    '<br>Created By: ' + issue.createdBy
                                ).join('<br><br>');
                                tooltip.style.display = 'block';
                                tooltip.style.left = event.offsetX + 10 + 'px';
                                tooltip.style.top = event.offsetY + 10 + 'px';
                            } else {
                                tooltip.style.display = 'none';
                            }
                        }
                    }
                });

                // Update counts for each segment
                document.getElementById(countLabels[0]).innerHTML = '<span class="color-box" style="background-color: rgba(255, 99, 132, 1);"></span> Last 7 Days: ' + data.datasets[0].data[0];
                document.getElementById(countLabels[1]).innerHTML = '<span class="color-box" style="background-color: rgba(54, 162, 235, 1);"></span> Last 1 Month: ' + data.datasets[0].data[1];
                document.getElementById(countLabels[2]).innerHTML = '<span class="color-box" style="background-color: rgba(255, 206, 86, 1);"></span> More Than 1 Month: ' + data.datasets[0].data[2];

                return chart;
            }

            createChart(androidCtx, androidData, [
                %s, // Last 7 Days issues
                %s, // Last 1 Month issues
                %s  // More Than 1 Month issues
            ], 'android-tooltip', ['android-count-last7', 'android-count-last1month', 'android-count-morethan1month']);

            createChart(iosCtx, iosData, [
                %s, // Last 7 Days issues
                %s, // Last 1 Month issues
                %s  // More Than 1 Month issues
            ], 'ios-tooltip', ['ios-count-last7', 'ios-count-last1month', 'ios-count-morethan1month']);
        </script>
    </body>
    </html>
""",
                androidTitle, iosTitle,
                androidLast7Days.size(), androidLastMonth.size(), androidMoreThanOneMonth.size(),
                iosLast7Days.size(), iosLastMonth.size(), iosMoreThanOneMonth.size(),
                androidTooltipLast7Days, androidTooltipLastMonth, androidTooltipMoreThanOneMonth,
                iosTooltipLast7Days, iosTooltipLastMonth, iosTooltipMoreThanOneMonth
        );

        // Specify the file path where the dashboard will be saved
        File htmlFile = new File("dashboard.html");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFile))) {
            writer.write(htmlContent);
        }

        System.out.println("Dashboard generated successfully! Open 'dashboard.html' to view.");

        // Open the dashboard in the default web browser
        openDashboardInChrome(htmlFile);
    }

    private String buildTooltipData(List<GHIssue> issues, String platform, String period) throws IOException {
        StringBuilder tooltipData = new StringBuilder();
        tooltipData.append("{ total: ").append(issues.size()).append(", details: [");

        for (GHIssue issue : issues) {
            tooltipData.append(String.format("{ id: %d, title: '%s', createdAt: '%s', createdBy: '%s' },",
                    issue.getNumber(), issue.getTitle(), issue.getCreatedAt(), issue.getUser().getLogin()));
        }

        if (tooltipData.length() > 1) {
            tooltipData.setLength(tooltipData.length() - 1); // Remove last comma
        }
        tooltipData.append("] }");
        return tooltipData.toString();

    }

    private void openDashboardInChrome(File htmlFile) {
        String os = System.getProperty("os.name").toLowerCase();
        String chromePath;

        // Determine the path to Chrome based on the OS
        if (os.contains("win")) {
            chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"; // Windows
        } else if (os.contains("mac")) {
            chromePath = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"; // macOS
        } else if (os.contains("nix") || os.contains("nux")) {
            chromePath = "google-chrome"; // Linux, assuming it's in PATH
        } else {
            System.err.println("Unsupported operating system. Cannot open Chrome.");
            return;
        }

        // Create the command to open Chrome with the HTML file
        ProcessBuilder processBuilder = new ProcessBuilder(chromePath, htmlFile.getAbsolutePath());
        try {
            processBuilder.start();
        } catch (IOException e) {
            System.err.println("Failed to open the dashboard in Chrome: " + e.getMessage());
        }
    }
}

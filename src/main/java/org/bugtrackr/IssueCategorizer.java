package org.bugtrackr;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.bugtrackr.HtmlDashboardGenerator.*;

public class IssueCategorizer {
    private final String accessToken;

    public IssueCategorizer(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<GHIssue> fetchOpenBugs(String repositoryName) throws IOException {
        GitHub github = GitHub.connectUsingOAuth(accessToken);
        GHRepository repository = github.getRepository(repositoryName);

        // Fetch all open issues
        List<GHIssue> allOpenIssues = repository.getIssues(GHIssueState.OPEN);

        // Filter issues that are labeled with "bug" (case-insensitive)
        List<GHIssue> openBugs = allOpenIssues.stream()
                .filter(issue -> issue.getLabels().stream()
                        .anyMatch(label -> label.getName().equalsIgnoreCase("bug")))
                .toList();

        return openBugs;
    }

    public void categorizeAndGenerateDashboard(List<GHIssue> androidIssues, List<GHIssue> iosIssues) throws IOException {
        String androidTitle = "Android Issues";
        String iosTitle = "iOS Issues";

        List<GHIssue> androidLast7Days = categorizeIssues(androidIssues, 7, true);
        List<GHIssue> androidLastMonth = categorizeIssues(androidIssues, 30, false);
        List<GHIssue> androidMoreThanOneMonth = categorizeIssues(androidIssues, Integer.MAX_VALUE, false);

        List<GHIssue> iosLast7Days = categorizeIssues(iosIssues, 7, true);
        List<GHIssue> iosLastMonth = categorizeIssues(iosIssues, 30, false);
        List<GHIssue> iosMoreThanOneMonth = categorizeIssues(iosIssues, Integer.MAX_VALUE, false);

        new HtmlDashboardGenerator().generateHtmlFile(androidLast7Days, androidLastMonth, androidMoreThanOneMonth, androidTitle,
                iosLast7Days, iosLastMonth, iosMoreThanOneMonth, iosTitle);
    }

    private List<GHIssue> categorizeIssues(List<GHIssue> issues, int days, boolean isLast7Days) throws IOException {
        LocalDate today = LocalDate.now();
        List<GHIssue> filteredIssues = new ArrayList<>();

        for (GHIssue issue : issues) {
            LocalDate createdAt = issue.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (isLast7Days) {
                if (createdAt.isAfter(today.minusDays(days))) {
                    filteredIssues.add(issue);
                }
            } else {
                if (days == 30 && !filteredIssues.contains(issue) && createdAt.isBefore(today.minusDays(7)) && createdAt.isAfter(today.minusDays(30))) {
                    filteredIssues.add(issue);
                } else if (days == Integer.MAX_VALUE && !filteredIssues.contains(issue) && createdAt.isBefore(today.minusDays(30))) {
                    filteredIssues.add(issue);
                }
            }
        }
        return filteredIssues;
    }
}

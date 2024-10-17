package org.bugtrackr;

import org.kohsuke.github.GHIssue;

import java.io.IOException;
import java.util.List;

public class IssueDashboardGeneratorMain {
    public static void main(String[] args) {
        String accessToken = ""; // Replace with your GitHub access token
        IssueCategorizer dashboardCategorizer = new IssueCategorizer(accessToken);
        String repositoryNameAndroid = "zopsmart/android-consumer-app"; // Add your GitHub repo here (e.g., "owner/repo")
        String repositoryNameIOS = "zopsmart/ios-consumer-app"; // Add your GitHub repo here (e.g., "owner/repo")

        try {
            List<GHIssue> androidIssues = dashboardCategorizer.fetchOpenBugs(repositoryNameAndroid);
            List<GHIssue> iosIssues = dashboardCategorizer.fetchOpenBugs(repositoryNameIOS);

            dashboardCategorizer.categorizeAndGenerateDashboard(androidIssues, iosIssues);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception
        }
    }
}


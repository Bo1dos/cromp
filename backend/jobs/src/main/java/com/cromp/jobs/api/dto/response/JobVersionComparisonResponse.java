package com.cromp.jobs.api.dto.response;

import java.util.List;

public record JobVersionComparisonResponse(
        int fromVersion,
        int toVersion,
        List<JobVersionDiff> diffs,
        Summary summary
) {
    public record JobVersionDiff(String field, String oldValue, String newValue, String type) {}
    public record Summary(int totalChanges, int breakingChanges, List<String> addedSecrets) {}
}
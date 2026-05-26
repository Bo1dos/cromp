package com.cromp.executions.domain.model.exceptions;

public class ArtifactNotFoundException extends RuntimeException {
    public ArtifactNotFoundException(Long id) {
        super("ExecutionArtifact not found: id=" + id);
    }
}
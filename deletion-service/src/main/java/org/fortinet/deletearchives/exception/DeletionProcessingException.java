package org.fortinet.deletearchives.exception;

public class DeletionProcessingException extends RuntimeException {
    public DeletionProcessingException(String failedToProcessDeletionTask, Exception e) {
        super(failedToProcessDeletionTask, e);
    }
}

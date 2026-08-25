package com.syncstream.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferLogEntry {
    private String id;
    private Instant timestamp;
    private String level; // "INFO", "SUCCESS", "WARN", "ERROR"
    private String message;
    private String details;
}

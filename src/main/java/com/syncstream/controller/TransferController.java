package com.syncstream.controller;

import com.syncstream.model.TransferJob;
import com.syncstream.model.TransferRequest;
import com.syncstream.service.TransferEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferEngineService transferEngineService;

    @PostMapping("/start")
    public ResponseEntity<TransferJob> startTransfer(@RequestBody TransferRequest request) {
        log.info("Starting transfer request for Spotify playlist: {}", request.getSpotifyPlaylistId());
        TransferJob job = transferEngineService.startTransfer(request);
        return ResponseEntity.ok(job);
    }

    @GetMapping(value = "/progress/{jobId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable("jobId") String jobId) {
        log.info("SSE subscription requested for job: {}", jobId);
        return transferEngineService.subscribe(jobId);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<TransferJob> getJob(@PathVariable("jobId") String jobId) {
        return transferEngineService.getJob(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<TransferJob>> getAllJobs() {
        return ResponseEntity.ok(transferEngineService.getAllJobs());
    }
}

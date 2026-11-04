package it.cityvoice.api.features.stats.controllers;

import it.cityvoice.api.features.stats.dto.PublicStatsResponse;
import it.cityvoice.api.features.stats.services.PublicStatsServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cityvoice/public/stats")
@RequiredArgsConstructor
public class PublicStatsController {

    private final PublicStatsServ publicStatsServ;

    @GetMapping
    public ResponseEntity<PublicStatsResponse> getStats() {
        return ResponseEntity.ok(publicStatsServ.getStats());
    }
}

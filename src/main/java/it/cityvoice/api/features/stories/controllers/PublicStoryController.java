package it.cityvoice.api.features.stories.controllers;

import it.cityvoice.api.features.stories.dto.StoryCardResponse;
import it.cityvoice.api.features.stories.services.StoryQueryServ;
import it.cityvoice.api.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cityvoice/public/stories")
@RequiredArgsConstructor
public class PublicStoryController {

    private final StoryQueryServ storyQueryServ;

    @GetMapping
    public ResponseEntity<PageResponse<StoryCardResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(storyQueryServ.search(q, category, page, size));
    }
}

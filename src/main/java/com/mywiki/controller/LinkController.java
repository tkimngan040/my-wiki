package com.mywiki.controller;

import com.mywiki.model.dto.LinkRequest;
import com.mywiki.model.dto.LinkResponse;
import com.mywiki.model.dto.PaginatedLinkResponse;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.LinkService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/pages/{sourcePageId}/links")
public class LinkController {
    private final LinkService linkService;
    private final UserRepository userRepository;

    public LinkController(LinkService linkService, UserRepository userRepository) {
        this.linkService = linkService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public PaginatedLinkResponse getLinks(
            @PathVariable Integer workspaceId,
            @PathVariable Integer sourcePageId,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return linkService.getLinks(
                workspaceId,
                sourcePageId,
                getCurrentUserId(authentication),
                page,
                size,
                sort,
                direction
        );
    }

    @PostMapping
    public ResponseEntity<LinkResponse> createLink(@PathVariable Integer workspaceId,
            @PathVariable Integer sourcePageId, @Valid @RequestBody LinkRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                linkService.createLink(getRequiredUserId(authentication), workspaceId, sourcePageId, request));
    }

    @DeleteMapping("/{linkId}")
    public ResponseEntity<Void> deleteLink(@PathVariable Integer workspaceId,
            @PathVariable Integer sourcePageId, @PathVariable Integer linkId,
            Authentication authentication) {
        linkService.deleteLink(getRequiredUserId(authentication), workspaceId, sourcePageId, linkId);
        return ResponseEntity.noContent().build();
    }

    private Integer getCurrentUserId(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByEmail(authentication.getName()).map(User::getUserId).orElse(null);
    }

    private Integer getRequiredUserId(Authentication authentication) {
        Integer id = getCurrentUserId(authentication);
        if (id == null) throw new IllegalStateException("Authenticated user not found");
        return id;
    }
}

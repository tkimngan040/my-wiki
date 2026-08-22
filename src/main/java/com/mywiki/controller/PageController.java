package com.mywiki.controller;

import com.mywiki.model.dto.PageRequest;
import com.mywiki.model.dto.PageResponse;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.PageService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/pages")
public class PageController {
    private final PageService pageService;
    private final UserRepository userRepository;

    public PageController(PageService pageService, UserRepository userRepository) {
        this.pageService = pageService;
        this.userRepository = userRepository;
    }
    @GetMapping
    public List<PageResponse> getPages(@PathVariable Integer workspaceId, Authentication authentication) {
        return pageService.getVisiblePages(workspaceId, getCurrentUserId(authentication));
    }
    @GetMapping("/{pageId}")
    public PageResponse getPage(@PathVariable Integer workspaceId, @PathVariable Integer pageId,
                                Authentication authentication) {
        return pageService.getPage(workspaceId, pageId, getCurrentUserId(authentication));
    }
    @PostMapping
    public ResponseEntity<PageResponse> createPage(@PathVariable Integer workspaceId,
            @Valid @RequestBody PageRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                pageService.createPage(getRequiredUserId(authentication), workspaceId, request));
    }
    @PutMapping("/{pageId}")
    public PageResponse updatePage(@PathVariable Integer workspaceId, @PathVariable Integer pageId,
            @Valid @RequestBody PageRequest request, Authentication authentication) {
        return pageService.updatePage(getRequiredUserId(authentication), workspaceId, pageId, request);
    }
    @DeleteMapping("/{pageId}")
    public ResponseEntity<Void> deletePage(@PathVariable Integer workspaceId, @PathVariable Integer pageId,
                                           Authentication authentication) {
        pageService.deletePage(getRequiredUserId(authentication), workspaceId, pageId);
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

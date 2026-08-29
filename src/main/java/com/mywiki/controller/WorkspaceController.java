package com.mywiki.controller;

import com.mywiki.model.dto.PaginatedWorkspaceResponse;
import com.mywiki.model.dto.WorkspaceRequest;
import com.mywiki.model.dto.WorkspaceResponse;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final UserRepository userRepository;

    public WorkspaceController(WorkspaceService workspaceService, UserRepository userRepository) {
        this.workspaceService = workspaceService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public PaginatedWorkspaceResponse getWorkspaces(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return workspaceService.getVisibleWorkspaces(getCurrentUserId(authentication), page, size, sort, direction);
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceResponse getWorkspace(
            @PathVariable Integer workspaceId,
            Authentication authentication
    ) {
        return workspaceService.getWorkspace(workspaceId, getCurrentUserId(authentication));
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody WorkspaceRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.createWorkspace(getRequiredUserId(authentication), request));
    }

    @PutMapping("/{workspaceId}")
    public WorkspaceResponse updateWorkspace(
            @PathVariable Integer workspaceId,
            @Valid @RequestBody WorkspaceRequest request,
            Authentication authentication
    ) {
        return workspaceService.updateWorkspace(
                getRequiredUserId(authentication), workspaceId, request
        );
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Integer workspaceId,
            Authentication authentication
    ) {
        workspaceService.deleteWorkspace(getRequiredUserId(authentication), workspaceId);
        return ResponseEntity.noContent().build();
    }

    private Integer getCurrentUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName())
                .map(User::getUserId)
                .orElse(null);
    }

    private Integer getRequiredUserId(Authentication authentication) {
        Integer userId = getCurrentUserId(authentication);
        if (userId == null) {
            throw new IllegalStateException("Authenticated user not found");
        }
        return userId;
    }
}

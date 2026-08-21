package com.mywiki.controller;

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

import java.util.List;

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
    public List<WorkspaceResponse> getWorkspaces(Authentication authentication) {
        return workspaceService.getVisibleWorkspaces(getCurrentUserId(authentication));
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

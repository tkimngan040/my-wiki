package com.mywiki.controller;

import com.mywiki.model.dto.FolderRequest;
import com.mywiki.model.dto.FolderResponse;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.FolderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/folders")
public class FolderController {

    private final FolderService folderService;
    private final UserRepository userRepository;

    public FolderController(FolderService folderService, UserRepository userRepository) {
        this.folderService = folderService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<FolderResponse> getFolders(
            @PathVariable Integer workspaceId,
            Authentication authentication
    ) {
        return folderService.getVisibleFolders(workspaceId, getCurrentUserId(authentication));
    }

    @GetMapping("/{folderId}")
    public FolderResponse getFolder(
            @PathVariable Integer workspaceId,
            @PathVariable Integer folderId,
            Authentication authentication
    ) {
        return folderService.getFolder(workspaceId, folderId, getCurrentUserId(authentication));
    }

    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(
            @PathVariable Integer workspaceId,
            @Valid @RequestBody FolderRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                folderService.createFolder(getRequiredUserId(authentication), workspaceId, request)
        );
    }

    @PutMapping("/{folderId}")
    public FolderResponse updateFolder(
            @PathVariable Integer workspaceId,
            @PathVariable Integer folderId,
            @Valid @RequestBody FolderRequest request,
            Authentication authentication
    ) {
        return folderService.updateFolder(
                getRequiredUserId(authentication), workspaceId, folderId, request
        );
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable Integer workspaceId,
            @PathVariable Integer folderId,
            Authentication authentication
    ) {
        folderService.deleteFolder(getRequiredUserId(authentication), workspaceId, folderId);
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

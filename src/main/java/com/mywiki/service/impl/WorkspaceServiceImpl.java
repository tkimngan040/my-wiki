package com.mywiki.service.impl;

import com.mywiki.exception.WorkspaceAccessDeniedException;
import com.mywiki.exception.WorkspaceNameAlreadyExistsException;
import com.mywiki.exception.WorkspaceNotFoundException;
import com.mywiki.model.dto.WorkspaceRequest;
import com.mywiki.model.dto.WorkspaceResponse;
import com.mywiki.model.entity.User;
import com.mywiki.model.entity.Workspace;
import com.mywiki.repository.UserRepository;
import com.mywiki.repository.FolderRepository;
import com.mywiki.repository.LinkRepository;
import com.mywiki.repository.PageRepository;
import com.mywiki.repository.WorkspaceRepository;
import com.mywiki.service.interfaces.WorkspaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private static final String PUBLIC = "Public";
    private static final String PRIVATE = "Private";

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;
    private final PageRepository pageRepository;
    private final FolderRepository folderRepository;

    public WorkspaceServiceImpl(
            WorkspaceRepository workspaceRepository,
            UserRepository userRepository,
            LinkRepository linkRepository,
            PageRepository pageRepository,
            FolderRepository folderRepository
    ) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.linkRepository = linkRepository;
        this.pageRepository = pageRepository;
        this.folderRepository = folderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getVisibleWorkspaces(Integer userId) {
        List<Workspace> workspaces = userId == null
                ? workspaceRepository.findByVisibilityOrderByUpdatedAtDesc(PUBLIC)
                : workspaceRepository.findByVisibilityOrOwner_UserIdOrderByUpdatedAtDesc(PUBLIC, userId);

        return workspaces.stream().map(WorkspaceResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Integer workspaceId, Integer userId) {
        Workspace workspace = findWorkspace(workspaceId);
        if (PRIVATE.equals(workspace.getVisibility())
                && !isOwner(workspace, userId)) {
            throw new WorkspaceAccessDeniedException("You do not have access to this workspace");
        }
        return WorkspaceResponse.from(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(Integer userId, WorkspaceRequest request) {
        User owner = findUser(userId);
        String visibility = normalizeVisibility(request.getVisibility());
        if (workspaceRepository.existsByOwner_UserIdAndNameIgnoreCase(userId, request.getName().trim())) {
            throw new WorkspaceNameAlreadyExistsException("Workspace name already exists");
        }

        Workspace workspace = new Workspace(
                owner,
                request.getName().trim(),
                request.getDescription(),
                visibility
        );
        return WorkspaceResponse.from(workspaceRepository.save(workspace));
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(
            Integer userId,
            Integer workspaceId,
            WorkspaceRequest request
    ) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        String name = request.getName().trim();
        if (workspaceRepository.existsByOwner_UserIdAndNameIgnoreCaseAndWorkspaceIdNot(
                userId, name, workspaceId
        )) {
            throw new WorkspaceNameAlreadyExistsException("Workspace name already exists");
        }

        workspace.setName(name);
        workspace.setDescription(request.getDescription());
        workspace.setVisibility(normalizeVisibility(request.getVisibility()));
        return WorkspaceResponse.from(workspaceRepository.save(workspace));
    }

    @Override
    @Transactional
    public void deleteWorkspace(Integer userId, Integer workspaceId) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        linkRepository.deleteAllByWorkspaceId(workspaceId);
        pageRepository.deleteAllByWorkspaceId(workspaceId);
        folderRepository.deleteAllByWorkspaceId(workspaceId);
        workspaceRepository.delete(workspace);
    }

    private Workspace findWorkspace(Integer workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new WorkspaceNotFoundException("User not found"));
    }

    private boolean isOwner(Workspace workspace, Integer userId) {
        return userId != null && workspace.getOwner().getUserId().equals(userId);
    }

    private void requireOwner(Workspace workspace, Integer userId) {
        if (!isOwner(workspace, userId)) {
            throw new WorkspaceAccessDeniedException("Only the workspace owner can modify it");
        }
    }

    private String normalizeVisibility(String visibility) {
        if (PUBLIC.equalsIgnoreCase(visibility)) {
            return PUBLIC;
        }
        if (PRIVATE.equalsIgnoreCase(visibility)) {
            return PRIVATE;
        }
        throw new IllegalArgumentException("Visibility must be Public or Private");
    }
}

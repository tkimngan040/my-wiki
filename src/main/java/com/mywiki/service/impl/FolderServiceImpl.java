package com.mywiki.service.impl;

import com.mywiki.exception.*;
import com.mywiki.model.dto.FolderRequest;
import com.mywiki.model.dto.FolderResponse;
import com.mywiki.model.entity.Folder;
import com.mywiki.model.entity.User;
import com.mywiki.model.entity.Workspace;
import com.mywiki.repository.FolderRepository;
import com.mywiki.repository.LinkRepository;
import com.mywiki.repository.PageRepository;
import com.mywiki.repository.WorkspaceRepository;
import com.mywiki.service.interfaces.FolderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PageRepository pageRepository;
    private final LinkRepository linkRepository;

    public FolderServiceImpl(
            FolderRepository folderRepository,
            WorkspaceRepository workspaceRepository,
            PageRepository pageRepository,
            LinkRepository linkRepository
    ) {
        this.folderRepository = folderRepository;
        this.workspaceRepository = workspaceRepository;
        this.pageRepository = pageRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getVisibleFolders(Integer workspaceId, Integer userId) {
        Workspace workspace = findWorkspace(workspaceId);
        requireViewAccess(workspace, userId);
        return folderRepository.findAllByWorkspaceWorkspaceId(workspaceId).stream()
                .map(FolderResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FolderResponse getFolder(Integer workspaceId, Integer folderId, Integer userId) {
        Workspace workspace = findWorkspace(workspaceId);
        requireViewAccess(workspace, userId);
        return FolderResponse.from(findFolder(workspaceId, folderId));
    }

    @Override
    @Transactional
    public FolderResponse createFolder(Integer userId, Integer workspaceId, FolderRequest request) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        Folder parent = findParent(workspaceId, request.getParentFolderId());
        String name = request.getName().trim();
        requireUniqueName(workspaceId, parent, name, null);

        return FolderResponse.from(folderRepository.save(
                new Folder(workspace, parent, name, request.getDescription())
        ));
    }

    @Override
    @Transactional
    public FolderResponse updateFolder(
            Integer userId, Integer workspaceId, Integer folderId, FolderRequest request
    ) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        Folder folder = findFolder(workspaceId, folderId);
        Folder parent = findParent(workspaceId, request.getParentFolderId());

        if (parent != null && isDescendant(parent, folder, workspaceId)) {
            throw new InvalidFolderMoveException("A folder cannot be moved into its own descendant");
        }

        String name = request.getName().trim();
        requireUniqueName(workspaceId, parent, name, folderId);
        folder.setName(name);
        folder.setDescription(request.getDescription());
        folder.setParentFolder(parent);
        return FolderResponse.from(folderRepository.save(folder));
    }

    @Override
    @Transactional
    public void deleteFolder(Integer userId, Integer workspaceId, Integer folderId) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        Folder root = findFolder(workspaceId, folderId);
        List<Folder> folders = folderRepository.findAllByWorkspaceWorkspaceId(workspaceId);
        Set<Integer> folderIds = collectSubtreeIds(root, folders);

        List<Integer> pageIds = pageRepository.findAll().stream()
                .filter(page -> page.getFolder() != null
                        && folderIds.contains(page.getFolder().getFolderId()))
                .map(page -> page.getPageId())
                .toList();
        if (!pageIds.isEmpty()) {
            linkRepository.deleteAllByPageIds(pageIds);
            pageRepository.deleteAllByFolderIds(folderIds);
        }
        List<Folder> foldersToDelete = folders.stream()
                .filter(folder -> folderIds.contains(folder.getFolderId()))
                .sorted(Comparator.comparingInt(this::depth).reversed())
                .toList();
        folderRepository.deleteAll(foldersToDelete);
    }

    private Workspace findWorkspace(Integer workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
    }

    private Folder findFolder(Integer workspaceId, Integer folderId) {
        return folderRepository.findAllByWorkspaceWorkspaceId(workspaceId).stream()
                .filter(folder -> folder.getFolderId().equals(folderId))
                .findFirst()
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));
    }

    private Folder findParent(Integer workspaceId, Integer parentFolderId) {
        if (parentFolderId == null) {
            return null;
        }
        return findFolder(workspaceId, parentFolderId);
    }

    private void requireViewAccess(Workspace workspace, Integer userId) {
        if ("Private".equals(workspace.getVisibility())
                && (userId == null || !workspace.getOwner().getUserId().equals(userId))) {
            throw new FolderAccessDeniedException("You do not have access to this workspace");
        }
    }

    private void requireOwner(Workspace workspace, Integer userId) {
        if (userId == null || !workspace.getOwner().getUserId().equals(userId)) {
            throw new FolderAccessDeniedException("Only the workspace owner can modify folders");
        }
    }

    private void requireUniqueName(Integer workspaceId, Folder parent, String name, Integer folderId) {
        boolean duplicate = parent == null
                ? folderId == null
                    ? folderRepository.existsByWorkspaceWorkspaceIdAndNameIgnoreCaseAndParentFolderIsNull(workspaceId, name)
                    : folderRepository.existsByWorkspaceWorkspaceIdAndNameIgnoreCaseAndParentFolderIsNullAndFolderIdNot(workspaceId, name, folderId)
                : folderId == null
                    ? folderRepository.existsByWorkspaceWorkspaceIdAndParentFolderFolderIdAndNameIgnoreCase(workspaceId, parent.getFolderId(), name)
                    : folderRepository.existsByWorkspaceWorkspaceIdAndParentFolderFolderIdAndNameIgnoreCaseAndFolderIdNot(workspaceId, parent.getFolderId(), name, folderId);
        if (duplicate) {
            throw new FolderNameAlreadyExistsException("Folder name already exists at this level");
        }
    }

    private boolean isDescendant(Folder candidateParent, Folder folder, Integer workspaceId) {
        Folder current = candidateParent;
        while (current != null) {
            if (current.getFolderId().equals(folder.getFolderId())) {
                return true;
            }
            current = current.getParentFolder();
        }
        return false;
    }

    private Set<Integer> collectSubtreeIds(Folder root, List<Folder> folders) {
        Map<Integer, List<Folder>> children = folders.stream()
                .filter(folder -> folder.getParentFolder() != null)
                .collect(Collectors.groupingBy(folder -> folder.getParentFolder().getFolderId()));
        Set<Integer> result = new HashSet<>();
        Deque<Integer> pending = new ArrayDeque<>();
        pending.push(root.getFolderId());
        while (!pending.isEmpty()) {
            Integer current = pending.pop();
            if (result.add(current)) {
                children.getOrDefault(current, List.of())
                        .forEach(child -> pending.push(child.getFolderId()));
            }
        }
        return result;
    }

    private int depth(Folder folder) {
        int depth = 0;
        Folder current = folder.getParentFolder();
        while (current != null) {
            depth++;
            current = current.getParentFolder();
        }
        return depth;
    }
}

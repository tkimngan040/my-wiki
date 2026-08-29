package com.mywiki.service.impl;

import com.mywiki.exception.*;
import com.mywiki.model.dto.PageRequest;
import com.mywiki.model.dto.PageResponse;
import com.mywiki.model.dto.PaginatedPageResponse;
import com.mywiki.model.entity.*;
import com.mywiki.repository.*;
import com.mywiki.service.interfaces.PageService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PageServiceImpl implements PageService {
    private final PageRepository pageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final FolderRepository folderRepository;
    private final LinkRepository linkRepository;

    public PageServiceImpl(PageRepository pageRepository, WorkspaceRepository workspaceRepository,
                           FolderRepository folderRepository, LinkRepository linkRepository) {
        this.pageRepository = pageRepository;
        this.workspaceRepository = workspaceRepository;
        this.folderRepository = folderRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedPageResponse getVisiblePages(Integer workspaceId, Integer userId, int page, int size, String sort, String direction) {
        Workspace workspace = findWorkspace(workspaceId);
        requireViewAccess(workspace, userId);
        validatePaging(page, size, sort, direction);

        String normalizedSort = normalizeSort(sort);
        Sort sortSpec = Sort.by(Sort.Direction.fromString(direction), normalizedSort);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortSpec);

        org.springframework.data.domain.Page<com.mywiki.model.entity.Page> result = pageRepository.findAllByWorkspaceWorkspaceId(workspaceId, pageable);
        return new PaginatedPageResponse(
                result.getContent().stream().map(PageResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                normalizedSort,
                direction.toLowerCase()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse getPage(Integer workspaceId, Integer pageId, Integer userId) {
        Workspace workspace = findWorkspace(workspaceId);
        requireViewAccess(workspace, userId);
        return PageResponse.from(findPage(workspaceId, pageId));
    }

    @Override
    @Transactional
    public PageResponse createPage(Integer userId, Integer workspaceId, PageRequest request) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        Folder folder = findFolder(workspaceId, request.getFolderId());
        String title = request.getTitle().trim();
        if (pageRepository.existsByWorkspaceWorkspaceIdAndTitleIgnoreCase(workspaceId, title)) {
            throw new PageNameAlreadyExistsException("Page title already exists in this workspace");
        }
        return PageResponse.from(pageRepository.save(
                new com.mywiki.model.entity.Page(workspace, folder, title, request.getContent())
        ));
    }

    @Override
    @Transactional
    public PageResponse updatePage(Integer userId, Integer workspaceId, Integer pageId, PageRequest request) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        com.mywiki.model.entity.Page page = findPage(workspaceId, pageId);
        Folder folder = findFolder(workspaceId, request.getFolderId());
        String title = request.getTitle().trim();
        if (pageRepository.existsByWorkspaceWorkspaceIdAndTitleIgnoreCaseAndPageIdNot(
                workspaceId, title, pageId)) {
            throw new PageNameAlreadyExistsException("Page title already exists in this workspace");
        }
        page.setTitle(title);
        page.setContent(request.getContent());
        page.setFolder(folder);
        return PageResponse.from(pageRepository.save(page));
    }

    @Override
    @Transactional
    public void deletePage(Integer userId, Integer workspaceId, Integer pageId) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        com.mywiki.model.entity.Page page = findPage(workspaceId, pageId);
        linkRepository.deleteAllByPageIds(List.of(pageId));
        pageRepository.delete(page);
    }

    private Workspace findWorkspace(Integer id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
    }
    private com.mywiki.model.entity.Page findPage(Integer workspaceId, Integer pageId) {
        return pageRepository.findAllByWorkspaceWorkspaceId(workspaceId).stream()
                .filter(page -> page.getPageId().equals(pageId)).findFirst()
                .orElseThrow(() -> new PageNotFoundException("Page not found"));
    }
    private Folder findFolder(Integer workspaceId, Integer folderId) {
        if (folderId == null) return null;
        return folderRepository.findAllByWorkspaceWorkspaceId(workspaceId).stream()
                .filter(folder -> folder.getFolderId().equals(folderId)).findFirst()
                .orElseThrow(() -> new FolderNotFoundException("Folder not found"));
    }
    private void requireViewAccess(Workspace workspace, Integer userId) {
        if ("Private".equals(workspace.getVisibility())
                && (userId == null || !workspace.getOwner().getUserId().equals(userId))) {
            throw new PageAccessDeniedException("You do not have access to this workspace");
        }
    }
    private void requireOwner(Workspace workspace, Integer userId) {
        if (userId == null || !workspace.getOwner().getUserId().equals(userId)) {
            throw new PageAccessDeniedException("Only the workspace owner can modify pages");
        }
    }

    private void validatePaging(int page, int size, String sort, String direction) {
        if (page < 0) throw new IllegalArgumentException("Page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Size must be between 1 and 100");
        if (direction == null || (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("Direction must be asc or desc");
        }
        if (sort == null || !isValidSort(sort)) throw new IllegalArgumentException("Sort must be one of: title, createdAt, updatedAt");
    }

    private boolean isValidSort(String sort) {
        return "title".equalsIgnoreCase(sort)
                || "createdAt".equalsIgnoreCase(sort)
                || "updatedAt".equalsIgnoreCase(sort);
    }

    private String normalizeSort(String sort) {
        if ("title".equalsIgnoreCase(sort)) return "title";
        if ("createdAt".equalsIgnoreCase(sort)) return "createdAt";
        return "updatedAt";
    }
}

package com.mywiki.service.impl;

import com.mywiki.exception.*;
import com.mywiki.model.dto.PageRequest;
import com.mywiki.model.dto.PageResponse;
import com.mywiki.model.entity.*;
import com.mywiki.repository.*;
import com.mywiki.service.interfaces.PageService;
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
    public List<PageResponse> getVisiblePages(Integer workspaceId, Integer userId) {
        Workspace workspace = findWorkspace(workspaceId);
        requireViewAccess(workspace, userId);
        return pageRepository.findAllByWorkspaceWorkspaceId(workspaceId).stream()
                .map(PageResponse::from).toList();
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
                new Page(workspace, folder, title, request.getContent())
        ));
    }

    @Override
    @Transactional
    public PageResponse updatePage(Integer userId, Integer workspaceId, Integer pageId, PageRequest request) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        Page page = findPage(workspaceId, pageId);
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
        Page page = findPage(workspaceId, pageId);
        linkRepository.deleteAllByPageIds(List.of(pageId));
        pageRepository.delete(page);
    }

    private Workspace findWorkspace(Integer id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
    }
    private Page findPage(Integer workspaceId, Integer pageId) {
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
}

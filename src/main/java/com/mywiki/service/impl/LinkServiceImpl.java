package com.mywiki.service.impl;

import com.mywiki.exception.*;
import com.mywiki.model.dto.LinkRequest;
import com.mywiki.model.dto.LinkResponse;
import com.mywiki.model.entity.*;
import com.mywiki.repository.*;
import com.mywiki.service.interfaces.LinkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class LinkServiceImpl implements LinkService {
    private final LinkRepository linkRepository;
    private final PageRepository pageRepository;
    private final WorkspaceRepository workspaceRepository;

    public LinkServiceImpl(LinkRepository linkRepository, PageRepository pageRepository,
                           WorkspaceRepository workspaceRepository) {
        this.linkRepository = linkRepository;
        this.pageRepository = pageRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkResponse> getLinks(Integer workspaceId, Integer sourcePageId, Integer userId) {
        Workspace workspace = findWorkspace(workspaceId);
        requireViewAccess(workspace, userId);
        findPage(workspaceId, sourcePageId);
        return linkRepository.findAllByWorkspaceWorkspaceIdAndSourcePagePageId(workspaceId, sourcePageId)
                .stream().map(LinkResponse::from).toList();
    }

    @Override
    @Transactional
    public LinkResponse createLink(Integer userId, Integer workspaceId, Integer sourcePageId,
                                   LinkRequest request) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        Page source = findPage(workspaceId, sourcePageId);
        Page target = findPage(workspaceId, request.getTargetPageId());
        if (source.getPageId().equals(target.getPageId())) {
            throw new InvalidLinkException("A page cannot link to itself");
        }
        Link link = new Link(source, target, request.getAnchorText().trim());
        return LinkResponse.from(linkRepository.save(link));
    }

    @Override
    @Transactional
    public void deleteLink(Integer userId, Integer workspaceId, Integer sourcePageId, Integer linkId) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        findPage(workspaceId, sourcePageId);
        Link link = linkRepository.findAllByWorkspaceWorkspaceIdAndSourcePagePageId(
                        workspaceId, sourcePageId).stream()
                .filter(item -> item.getLinkId().equals(linkId)).findFirst()
                .orElseThrow(() -> new LinkNotFoundException("Link not found"));
        linkRepository.delete(link);
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
    private void requireViewAccess(Workspace workspace, Integer userId) {
        if ("Private".equals(workspace.getVisibility())
                && (userId == null || !workspace.getOwner().getUserId().equals(userId))) {
            throw new LinkAccessDeniedException("You do not have access to this workspace");
        }
    }
    private void requireOwner(Workspace workspace, Integer userId) {
        if (userId == null || !workspace.getOwner().getUserId().equals(userId)) {
            throw new LinkAccessDeniedException("Only the workspace owner can modify links");
        }
    }
}

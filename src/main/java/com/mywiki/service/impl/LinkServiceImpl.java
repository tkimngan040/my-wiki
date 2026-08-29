package com.mywiki.service.impl;

import com.mywiki.exception.*;
import com.mywiki.model.dto.LinkRequest;
import com.mywiki.model.dto.LinkResponse;
import com.mywiki.model.dto.PaginatedLinkResponse;
import com.mywiki.model.entity.*;
import com.mywiki.repository.*;
import com.mywiki.service.interfaces.LinkService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public java.util.List<LinkResponse> getLinks(Integer workspaceId, Integer sourcePageId, Integer userId) {
        return getLinks(workspaceId, sourcePageId, userId, 0, 20, "createdAt", "desc").content();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedLinkResponse getLinks(Integer workspaceId, Integer sourcePageId, Integer userId, int page, int size, String sort, String direction) {
        Workspace workspace = findWorkspace(workspaceId);
        requireViewAccess(workspace, userId);
        findPage(workspaceId, sourcePageId);
        validatePaging(page, size, sort, direction);

        String normalizedSort = normalizeSort(sort);
        Sort sortSpec = Sort.by(Sort.Direction.fromString(direction), normalizedSort);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortSpec);

        org.springframework.data.domain.Page<Link> result = linkRepository.findAllByWorkspaceWorkspaceIdAndSourcePagePageId(workspaceId, sourcePageId, pageable);
        return new PaginatedLinkResponse(
                result.getContent().stream().map(LinkResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                normalizedSort,
                direction.toLowerCase()
        );
    }

    @Override
    @Transactional
    public LinkResponse createLink(Integer userId, Integer workspaceId, Integer sourcePageId,
                                   LinkRequest request) {
        Workspace workspace = findWorkspace(workspaceId);
        requireOwner(workspace, userId);
        com.mywiki.model.entity.Page source = findPage(workspaceId, sourcePageId);
        com.mywiki.model.entity.Page target = findPage(workspaceId, request.getTargetPageId());
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
    private com.mywiki.model.entity.Page findPage(Integer workspaceId, Integer pageId) {
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

    private void validatePaging(int page, int size, String sort, String direction) {
        if (page < 0) throw new IllegalArgumentException("Page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Size must be between 1 and 100");
        if (direction == null || (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("Direction must be asc or desc");
        }
        if (sort == null || !isValidSort(sort)) throw new IllegalArgumentException("Sort must be one of: createdAt, anchorText");
    }

    private boolean isValidSort(String sort) {
        return "createdAt".equalsIgnoreCase(sort)
                || "anchorText".equalsIgnoreCase(sort);
    }

    private String normalizeSort(String sort) {
        if ("anchorText".equalsIgnoreCase(sort)) return "anchorText";
        return "createdAt";
    }
}

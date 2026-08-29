package com.mywiki.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<com.mywiki.model.entity.Page, Integer> {

    @Modifying
    @Query("delete from Page page where page.workspace.workspaceId = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") Integer workspaceId);

    @Modifying
    @Query("delete from Page page where page.folder.folderId in :folderIds")
    void deleteAllByFolderIds(@Param("folderIds") Collection<Integer> folderIds);

    @Query("""
            select page.pageId
            from Page page
            where page.workspace.workspaceId = :workspaceId
              and page.folder.folderId in :folderIds
            """)
    List<Integer> findIdsByWorkspaceIdAndFolderIds(
            @Param("workspaceId") Integer workspaceId,
            @Param("folderIds") Collection<Integer> folderIds
    );

    List<com.mywiki.model.entity.Page> findAllByWorkspaceWorkspaceId(Integer workspaceId);
    org.springframework.data.domain.Page<com.mywiki.model.entity.Page> findAllByWorkspaceWorkspaceId(Integer workspaceId, Pageable pageable);
    boolean existsByWorkspaceWorkspaceIdAndTitleIgnoreCase(Integer workspaceId, String title);
    boolean existsByWorkspaceWorkspaceIdAndTitleIgnoreCaseAndPageIdNot(
            Integer workspaceId, String title, Integer pageId
    );
}
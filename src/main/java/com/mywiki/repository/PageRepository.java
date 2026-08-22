package com.mywiki.repository;

import com.mywiki.model.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Modifying
    @Query("delete from Page page where page.workspace.workspaceId = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") Integer workspaceId);

    @Modifying
    @Query("delete from Page page where page.folder.folderId in :folderIds")
    void deleteAllByFolderIds(@Param("folderIds") Collection<Integer> folderIds);

    List<Page> findAllByWorkspaceWorkspaceId(Integer workspaceId);
    boolean existsByWorkspaceWorkspaceIdAndTitleIgnoreCase(Integer workspaceId, String title);
    boolean existsByWorkspaceWorkspaceIdAndTitleIgnoreCaseAndPageIdNot(
            Integer workspaceId, String title, Integer pageId
    );
}
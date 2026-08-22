package com.mywiki.repository;

import com.mywiki.model.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Integer> {

    @Modifying
    @Query("delete from Folder folder where folder.workspace.workspaceId = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") Integer workspaceId);

    List<Folder> findAllByWorkspaceWorkspaceId(Integer workspaceId);

    boolean existsByWorkspaceWorkspaceIdAndParentFolderFolderIdAndNameIgnoreCase(
            Integer workspaceId, Integer parentFolderId, String name
    );

    boolean existsByWorkspaceWorkspaceIdAndNameIgnoreCaseAndParentFolderIsNull(
            Integer workspaceId, String name
    );

    boolean existsByWorkspaceWorkspaceIdAndParentFolderFolderIdAndNameIgnoreCaseAndFolderIdNot(
            Integer workspaceId, Integer parentFolderId, String name, Integer folderId
    );

    boolean existsByWorkspaceWorkspaceIdAndNameIgnoreCaseAndParentFolderIsNullAndFolderIdNot(
            Integer workspaceId, String name, Integer folderId
    );
}
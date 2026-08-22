package com.mywiki.repository;

import com.mywiki.model.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

@Repository
public interface LinkRepository extends JpaRepository<Link, Integer> {

    @Modifying
    @Query("delete from Link link where link.workspace.workspaceId = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") Integer workspaceId);

    @Modifying
    @Query("""
            delete from Link link
            where link.sourcePage.pageId in :pageIds
               or link.targetPage.pageId in :pageIds
            """)
    void deleteAllByPageIds(@Param("pageIds") Collection<Integer> pageIds);

    List<Link> findAllByWorkspaceWorkspaceIdAndSourcePagePageId(
            Integer workspaceId, Integer sourcePageId
    );
    List<Link> findAllByWorkspaceWorkspaceId(Integer workspaceId);
}
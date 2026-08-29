package com.mywiki.repository;

import com.mywiki.model.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer> {
    List<Workspace> findByVisibilityOrderByUpdatedAtDesc(String visibility);

    List<Workspace> findByVisibilityOrOwner_UserIdOrderByUpdatedAtDesc(
            String visibility,
            Integer ownerId
    );

    Page<Workspace> findByVisibility(String visibility, Pageable pageable);

    Page<Workspace> findByVisibilityOrOwner_UserId(String visibility, Integer ownerId, Pageable pageable);

    boolean existsByOwner_UserIdAndNameIgnoreCase(Integer ownerId, String name);

    boolean existsByOwner_UserIdAndNameIgnoreCaseAndWorkspaceIdNot(
            Integer ownerId,
            String name,
            Integer workspaceId
    );
}
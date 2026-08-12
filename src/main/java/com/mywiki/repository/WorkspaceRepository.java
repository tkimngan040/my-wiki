package com.mywiki.repository;

import com.mywiki.model.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer> {
}
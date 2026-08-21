package com.mywiki.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "Links",
        schema = "dbo"
)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LinkId")
    private Integer linkId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "WorkspaceId",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_Links_Workspace")
    )
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "SourcePageId",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_Links_SourcePage")
    )
    private Page sourcePage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "TargetPageId",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_Links_TargetPage")
    )
    private Page targetPage;

    @Column(name = "AnchorText", nullable = false, length = 200)
    private String anchorText;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    public Link() {
    }

    public Link(
            Page sourcePage,
            Page targetPage,
            String anchorText
    ) {
        this.workspace = sourcePage.getWorkspace();
        this.sourcePage = sourcePage;
        this.targetPage = targetPage;
        this.anchorText = anchorText;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Integer getLinkId() {
        return linkId;
    }

    public void setLinkId(Integer linkId) {
        this.linkId = linkId;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public Page getSourcePage() {
        return sourcePage;
    }

    public void setSourcePage(Page sourcePage) {
        this.sourcePage = sourcePage;
    }

    public Page getTargetPage() {
        return targetPage;
    }

    public void setTargetPage(Page targetPage) {
        this.targetPage = targetPage;
    }

    public String getAnchorText() {
        return anchorText;
    }

    public void setAnchorText(String anchorText) {
        this.anchorText = anchorText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
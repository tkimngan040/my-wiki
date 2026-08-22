package com.mywiki.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PageRequest {
    @NotBlank
    @Size(max = 200)
    private String title;
    private String content;
    private Integer folderId;

    public PageRequest() {
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getFolderId() { return folderId; }
    public void setFolderId(Integer folderId) { this.folderId = folderId; }
}

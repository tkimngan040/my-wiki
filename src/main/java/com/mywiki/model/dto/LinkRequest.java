package com.mywiki.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LinkRequest {
    @NotNull(message = "Target page ID is required")
    private Integer targetPageId;
    @NotBlank(message = "Anchor text must not be blank")
    @Size(max = 200, message = "Anchor text must not exceed 200 characters")
    private String anchorText;

    public LinkRequest() {
    }
    public Integer getTargetPageId() { return targetPageId; }
    public void setTargetPageId(Integer targetPageId) { this.targetPageId = targetPageId; }
    public String getAnchorText() { return anchorText; }
    public void setAnchorText(String anchorText) { this.anchorText = anchorText; }
}

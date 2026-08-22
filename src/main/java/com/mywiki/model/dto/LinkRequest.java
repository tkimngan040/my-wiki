package com.mywiki.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LinkRequest {
    @NotNull
    private Integer targetPageId;
    @NotBlank
    @Size(max = 200)
    private String anchorText;

    public LinkRequest() {
    }
    public Integer getTargetPageId() { return targetPageId; }
    public void setTargetPageId(Integer targetPageId) { this.targetPageId = targetPageId; }
    public String getAnchorText() { return anchorText; }
    public void setAnchorText(String anchorText) { this.anchorText = anchorText; }
}

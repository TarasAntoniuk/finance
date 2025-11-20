package com.tarasantoniuk.finance.common.dto;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private PageMetadata metadata;

    public PageResponse() {
    }

    public PageResponse(List<T> content, PageMetadata metadata) {
        this.content = content;
        this.metadata = metadata;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public PageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PageMetadata metadata) {
        this.metadata = metadata;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private List<T> content;
        private PageMetadata metadata;

        public Builder<T> content(List<T> content) {
            this.content = content;
            return this;
        }

        public Builder<T> metadata(PageMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public PageResponse<T> build() {
            return new PageResponse<>(content, metadata);
        }
    }
}
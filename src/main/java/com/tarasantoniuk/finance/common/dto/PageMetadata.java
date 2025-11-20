package com.tarasantoniuk.finance.common.dto;

public class PageMetadata {
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;

    public PageMetadata() {
    }

    public PageMetadata(int currentPage, int totalPages, int pageSize,
                        long totalElements, boolean hasNext, boolean hasPrevious) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int currentPage;
        private int totalPages;
        private int pageSize;
        private long totalElements;
        private boolean hasNext;
        private boolean hasPrevious;

        public Builder currentPage(int currentPage) {
            this.currentPage = currentPage;
            return this;
        }

        public Builder totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder totalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public Builder hasNext(boolean hasNext) {
            this.hasNext = hasNext;
            return this;
        }

        public Builder hasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
            return this;
        }

        public PageMetadata build() {
            return new PageMetadata(currentPage, totalPages, pageSize,
                    totalElements, hasNext, hasPrevious);
        }
    }
}
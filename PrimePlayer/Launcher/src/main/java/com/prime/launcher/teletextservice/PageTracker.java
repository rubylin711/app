// PageTracker.java
package com.prime.launcher.teletextservice;

public class PageTracker {
    private static PageTracker instance;
    private int currentPageNumber = 100; // 預設 page

    private PageTracker() {}

    public static synchronized PageTracker getInstance() {
        if (instance == null) {
            instance = new PageTracker();
        }
        return instance;
    }

    public synchronized void setCurrentPageNumber(int pageNumber) {
        currentPageNumber = pageNumber;
    }

    public synchronized int getCurrentPageNumber() {
        return currentPageNumber;
    }
} 

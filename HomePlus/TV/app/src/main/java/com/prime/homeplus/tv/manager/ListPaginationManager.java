package com.prime.homeplus.tv.manager;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ListPaginationManager<T> {
    private static final String TAG = "ListPaginationManager";
    private List<T> fullList;
    private int itemsPerPage;
    private int currentPage = 0;
    private int currentFocusIndex = 0;

    public ListPaginationManager(List<T> fullList, int itemsPerPage) {
        this.fullList = fullList;
        this.itemsPerPage = itemsPerPage;
    }

    public void updateData(List<T> newData) {
        if (newData == null) {
            fullList = new ArrayList<>();
        } else {
            fullList.clear();
            fullList.addAll(newData);
        }

        currentPage = 0;
        currentFocusIndex = 0;
    }

    public List<T> getFullData() {
        if (fullList == null || fullList.isEmpty()) {
            System.out.println("Full List is empty or null.");
            return new ArrayList<>(); // Return an empty list to avoid exceptions
        }

        return new ArrayList<>(fullList);
    }

    public List<T> getCurrentPageData() {
        // Check if fullList is null or empty
        if (fullList == null || fullList.isEmpty()) {
            System.out.println("Full List is empty or null.");
            return new ArrayList<>(); // Return an empty list to avoid exceptions
        }

        // Ensure currentPage is within valid bounds
        if (currentPage < 0) {
            System.out.println("Invalid currentPage index: " + currentPage + ". Resetting to first page.");
            currentPage = 0;
        } else if (currentPage > (fullList.size() - 1) / itemsPerPage) {
            System.out.println("Invalid currentPage index: " + currentPage + ". Resetting to last page.");
            currentPage = (fullList.size() - 1) / itemsPerPage;
        }

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, fullList.size()); // Ensure endIndex doesn't exceed fullList size

        // Return a new list to prevent modifications to the original list
        return new ArrayList<>(fullList.subList(startIndex, endIndex));
    }

    public void nextPage() {
        currentPage++;
        if (currentPage > (fullList.size() - 1) / itemsPerPage) { // If it exceeds the last page, go back to the first page
            currentPage = 0;
        }
    }

    public void previousPage() {
        currentPage--;
        if (currentPage < 0) { // If it goes below the first page, move to the last page
            currentPage = (fullList.size() - 1) / itemsPerPage;
        }
    }

    public int getTotalPages() {
        return (fullList.size() + itemsPerPage - 1) / itemsPerPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public T getCurrentFocusItem() {
        List<T> currentPageDataList = getCurrentPageData();
        if (currentPageDataList != null && currentPageDataList.size() > currentFocusIndex) {
            return currentPageDataList.get(currentFocusIndex);
        }

        return null;
    }

    public int getCurrentFocusIndex() {
        return currentFocusIndex;
    }

    public void setCurrentFocusIndex(int index) {
        currentFocusIndex = index;
    }

    public void setPageAndFocusByFullListIndex(int globalIndex) {
        if (globalIndex < 0 || globalIndex >= fullList.size()) {
            Log.d(TAG, "Invalid global index: " + globalIndex);
            return;
        }

        currentPage = globalIndex / itemsPerPage;
        currentFocusIndex = globalIndex % itemsPerPage;
    }
}

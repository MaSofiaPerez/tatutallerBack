package com.tatutaller.dto.response;

public class DashboardStatsResponse {

    private long totalUsers;
    private long totalBookings;
    private double totalRevenue;
    private long totalClasses;
    private long confirmedBookings;
    private long activeProducts;

    // Constructors
    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(long totalUsers, long totalBookings, double totalRevenue, long totalClasses) {
        this.totalUsers = totalUsers;
        this.totalBookings = totalBookings;
        this.totalRevenue = totalRevenue;
        this.totalClasses = totalClasses;
    }

    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(long totalClasses) {
        this.totalClasses = totalClasses;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public void setConfirmedBookings(long confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public long getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(long activeProducts) {
        this.activeProducts = activeProducts;
    }
}

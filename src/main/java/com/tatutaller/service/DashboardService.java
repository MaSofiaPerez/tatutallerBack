package com.tatutaller.service;

import com.tatutaller.dto.response.DashboardStatsResponse;
import com.tatutaller.entity.Booking;
import com.tatutaller.repository.BookingRepository;
import com.tatutaller.repository.ClassRepository;
import com.tatutaller.repository.ProductRepository;
import com.tatutaller.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ProductRepository productRepository;

    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.countUsers();
        long totalBookings = bookingRepository.countTotalBookings();
        Double revenue = bookingRepository.calculateTotalRevenue();
        double totalRevenue = revenue != null ? revenue : 0.0;
        long totalClasses = classRepository.countActiveClasses();
        long confirmedBookings = bookingRepository.countConfirmedBookings();
        long activeProducts = productRepository.countActiveProducts();

        DashboardStatsResponse response = new DashboardStatsResponse(totalUsers, totalBookings, totalRevenue,
                totalClasses);
        response.setConfirmedBookings(confirmedBookings);
        response.setActiveProducts(activeProducts);

        return response;
    }

    public List<Booking> getRecentBookings() {
        return bookingRepository.findRecentBookings();
    }
}

package com.tatutaller.repository;

import com.tatutaller.entity.Booking;
import com.tatutaller.entity.User;
import com.tatutaller.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);

    List<Booking> findByClassEntity(ClassEntity classEntity);

    // Buscar reservas por lista de clases (para profesores)
    List<Booking> findByClassEntityIn(List<ClassEntity> classes);

    List<Booking> findByStatus(Booking.BookingStatus status);

    List<Booking> findByBookingDate(LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM Booking b ORDER BY b.createdAt DESC")
    List<Booking> findRecentBookings();

    @Query("SELECT COUNT(b) FROM Booking b")
    long countTotalBookings();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
    long countConfirmedBookings();

    @Query("SELECT SUM(c.price) FROM Booking b JOIN b.classEntity c WHERE b.status = 'CONFIRMED'")
    Double calculateTotalRevenue();
}

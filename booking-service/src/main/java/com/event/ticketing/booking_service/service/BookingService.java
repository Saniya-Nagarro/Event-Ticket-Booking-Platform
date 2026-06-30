package com.event.ticketing.booking_service.service;

import com.event.ticketing.booking_service.dto.BookingRequestDTO;
import com.event.ticketing.booking_service.dto.BookingResponseDTO;

import java.util.List;

public interface BookingService {
    BookingResponseDTO bookTicket(BookingRequestDTO request);
    List<BookingResponseDTO> getMyBookings();
    BookingResponseDTO getBookingById(Long id);
    BookingResponseDTO cancelBooking(Long id);
    List<BookingResponseDTO> getAllBookings();
}

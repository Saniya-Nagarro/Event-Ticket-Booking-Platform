package com.event.ticketing.booking_service.service;

import java.util.List;

import com.event.ticketing.booking_service.dto.BookingRequestDTO;
import com.event.ticketing.booking_service.dto.BookingRequestV2DTO;
import com.event.ticketing.booking_service.dto.BookingResponseDTO;

public interface BookingService {
    BookingResponseDTO bookTicket(BookingRequestDTO request);
    BookingResponseDTO bookTicketV2(BookingRequestV2DTO request);
    List<BookingResponseDTO> getMyBookings();
    BookingResponseDTO getBookingById(Long id);
    BookingResponseDTO cancelBooking(Long id);
    List<BookingResponseDTO> getAllBookings();
}

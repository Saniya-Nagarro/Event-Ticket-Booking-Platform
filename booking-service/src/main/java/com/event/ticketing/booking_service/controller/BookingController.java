package com.event.ticketing.booking_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event.ticketing.booking_service.dto.BookingRequestDTO;
import com.event.ticketing.booking_service.dto.BookingRequestV2DTO;
import com.event.ticketing.booking_service.dto.BookingResponseDTO;
import com.event.ticketing.booking_service.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

	private final BookingService bookingService;

	@PostMapping
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<BookingResponseDTO> bookTicket(@Valid @RequestBody BookingRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookTicket(request));
	}

	@PostMapping("/v2")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<BookingResponseDTO> bookTicketV2(@Valid @RequestBody BookingRequestV2DTO request) {

		return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookTicketV2(request));
	}

	@GetMapping("/my")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<List<BookingResponseDTO>> getMyBookings() {
		return ResponseEntity.ok(bookingService.getMyBookings());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
	public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long id) {
		return ResponseEntity.ok(bookingService.getBookingById(id));
	}

	@PutMapping("/{id}/cancel")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable Long id) {
		return ResponseEntity.ok(bookingService.cancelBooking(id));
	}

	@GetMapping("/admin/all")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
		return ResponseEntity.ok(bookingService.getAllBookings());
	}
}

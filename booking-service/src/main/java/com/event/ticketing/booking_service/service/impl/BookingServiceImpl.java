package com.event.ticketing.booking_service.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.ticketing.booking_service.client.EventClient;
import com.event.ticketing.booking_service.dto.BookingRequestDTO;
import com.event.ticketing.booking_service.dto.BookingResponseDTO;
import com.event.ticketing.booking_service.dto.EventResponseDTO;
import com.event.ticketing.booking_service.entity.Booking;
import com.event.ticketing.booking_service.enums.BookingStatus;
import com.event.ticketing.booking_service.event.BookingCancelledEvent;
import com.event.ticketing.booking_service.event.BookingCreatedEvent;
import com.event.ticketing.booking_service.producer.BookingEventProducer;
import com.event.ticketing.booking_service.repository.BookingRepository;
import com.event.ticketing.booking_service.service.BookingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	private final BookingRepository bookingRepository;
	private final EventClient eventClient;
	private final BookingEventProducer bookingEventProducer;

	@Override
	@Transactional
	public BookingResponseDTO bookTicket(BookingRequestDTO request) {
		String loggedInEmail = getLoggedInEmail();

		log.info("Booking started userEmail={} eventId={} tickets={}", loggedInEmail, request.getEventId(),
				request.getNumberOfTickets());

		EventResponseDTO event = eventClient.getEventById(request.getEventId());

		if (event == null) {
			log.warn("Booking failed reason=event_not_found userEmail={} eventId={}", loggedInEmail,
					request.getEventId());
			throw new RuntimeException("Event not found");
		}

		if (!"PUBLISHED".equalsIgnoreCase(event.getStatus())) {
			log.warn("Booking failed reason=event_not_published userEmail={} eventId={} status={}", loggedInEmail,
					request.getEventId(), event.getStatus());
			throw new RuntimeException("Event is not available for booking");
		}

		if (event.getAvailableSeats() == null || event.getAvailableSeats() < request.getNumberOfTickets()) {
			log.warn(
					"Booking failed reason=not_enough_seats userEmail={} eventId={} requestedTickets={} availableSeats={}",
					loggedInEmail, request.getEventId(), request.getNumberOfTickets(), event.getAvailableSeats());
			throw new RuntimeException("Not enough seats available");
		}

		eventClient.reduceSeats(request.getEventId(), request.getNumberOfTickets());

		BigDecimal totalAmount = event.getTicketPrice().multiply(BigDecimal.valueOf(request.getNumberOfTickets()));

		Booking booking = Booking.builder().eventId(request.getEventId()).userEmail(loggedInEmail)
				.numberOfTickets(request.getNumberOfTickets()).totalAmount(totalAmount).status(BookingStatus.CONFIRMED)
				.build();

		bookingRepository.save(booking);

		log.info("Booking confirmed bookingId={} userEmail={} eventId={} tickets={} totalAmount={}", booking.getId(),
				booking.getUserEmail(), booking.getEventId(), booking.getNumberOfTickets(), booking.getTotalAmount());

		bookingEventProducer.publishBookingCreated(BookingCreatedEvent.builder().eventType("BOOKING_CREATED")
				.correlationId(MDC.get("correlationId")).bookingId(booking.getId()).eventId(booking.getEventId())
				.userEmail(booking.getUserEmail()).numberOfTickets(booking.getNumberOfTickets())
				.totalAmount(booking.getTotalAmount()).createdAt(booking.getCreatedAt()).build());

		log.info("Kafka event published eventType=BOOKING_CREATED bookingId={} eventId={} userEmail={}",
				booking.getId(), booking.getEventId(), booking.getUserEmail());

		return mapToResponse(booking);
	}

	@Override
	public List<BookingResponseDTO> getMyBookings() {
		String loggedInEmail = getLoggedInEmail();

		log.info("Fetching user bookings userEmail={}", loggedInEmail);

		List<BookingResponseDTO> bookings = bookingRepository.findByUserEmail(loggedInEmail).stream()
				.map(this::mapToResponse).toList();

		log.info("Fetched user bookings userEmail={} count={}", loggedInEmail, bookings.size());

		return bookings;
	}

	@Override
	public BookingResponseDTO getBookingById(Long id) {
		log.info("Fetching booking by id bookingId={}", id);

		Booking booking = getBooking(id);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		boolean isAdmin = authentication.getAuthorities().stream().anyMatch(
				auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

		if (!isAdmin && !booking.getUserEmail().equals(authentication.getName())) {
			log.warn("Booking access denied bookingId={} requestedBy={} owner={}", id, authentication.getName(),
					booking.getUserEmail());
			throw new AccessDeniedException("You can view only your own booking");
		}

		log.info("Booking fetched bookingId={} requestedBy={} status={}", id, authentication.getName(),
				booking.getStatus());

		return mapToResponse(booking);
	}

	@Override
	@Transactional
	public BookingResponseDTO cancelBooking(Long id) {
		String loggedInEmail = getLoggedInEmail();

		log.info("Booking cancellation started bookingId={} userEmail={}", id, loggedInEmail);

		Booking booking = getBooking(id);

		if (!booking.getUserEmail().equals(loggedInEmail)) {
			log.warn("Booking cancellation denied bookingId={} requestedBy={} owner={}", id, loggedInEmail,
					booking.getUserEmail());
			throw new AccessDeniedException("You can cancel only your own booking");
		}

		if (booking.getStatus() == BookingStatus.CANCELLED) {
			log.warn("Booking cancellation failed reason=already_cancelled bookingId={} userEmail={}", id,
					loggedInEmail);
			throw new RuntimeException("Booking already cancelled");
		}

		booking.setStatus(BookingStatus.CANCELLED);

		eventClient.increaseSeats(booking.getEventId(), booking.getNumberOfTickets());
		bookingRepository.save(booking);

		log.info("Booking cancelled bookingId={} userEmail={} eventId={} tickets={}", booking.getId(),
				booking.getUserEmail(), booking.getEventId(), booking.getNumberOfTickets());

		bookingEventProducer.publishBookingCancelled(
				BookingCancelledEvent.builder().eventType("BOOKING_CANCELLED").correlationId(MDC.get("correlationId"))
						.bookingId(booking.getId()).eventId(booking.getEventId()).userEmail(booking.getUserEmail())
						.numberOfTickets(booking.getNumberOfTickets()).cancelledAt(LocalDateTime.now()).build());

		log.info("Kafka event published eventType=BOOKING_CANCELLED bookingId={} eventId={} userEmail={}",
				booking.getId(), booking.getEventId(), booking.getUserEmail());

		return mapToResponse(booking);
	}

	@Override
	public List<BookingResponseDTO> getAllBookings() {
		log.info("Fetching all bookings");

		List<BookingResponseDTO> bookings = bookingRepository.findAll().stream().map(this::mapToResponse).toList();

		log.info("Fetched all bookings count={}", bookings.size());

		return bookings;
	}

	private String getLoggedInEmail() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	private Booking getBooking(Long id) {
		return bookingRepository.findById(id).orElseThrow(() -> {
			log.warn("Booking not found bookingId={}", id);
			return new RuntimeException("Booking not found");
		});
	}

	private BookingResponseDTO mapToResponse(Booking booking) {
		return BookingResponseDTO.builder().id(booking.getId()).eventId(booking.getEventId())
				.userEmail(booking.getUserEmail()).numberOfTickets(booking.getNumberOfTickets())
				.totalAmount(booking.getTotalAmount()).status(booking.getStatus()).createdAt(booking.getCreatedAt())
				.updatedAt(booking.getUpdatedAt()).build();
	}
}
package com.event.ticketing.booking_service.service.impl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.event.ticketing.booking_service.client.EventClient;
import com.event.ticketing.booking_service.dto.BookingRequestDTO;
import com.event.ticketing.booking_service.dto.BookingResponseDTO;
import com.event.ticketing.booking_service.dto.EventResponseDTO;
import com.event.ticketing.booking_service.entity.Booking;
import com.event.ticketing.booking_service.enums.BookingStatus;
import com.event.ticketing.booking_service.event.BookingCancelledEvent;
import com.event.ticketing.booking_service.event.BookingCreatedEvent;
import com.event.ticketing.booking_service.metrics.BookingMetrics;
import com.event.ticketing.booking_service.producer.BookingEventProducer;
import com.event.ticketing.booking_service.repository.BookingRepository;
import com.event.ticketing.booking_service.service.impl.BookingServiceImpl;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class BookingServiceImplTest {

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private EventClient eventClient;

	@Mock
	private BookingEventProducer bookingEventProducer;

	@InjectMocks
	private BookingServiceImpl bookingService;
	
    @Mock
    private BookingMetrics bookingMetrics;

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	private void mockCustomerLogin(String email) {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, null,
				List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));
	}

	private void mockAdminLogin(String email) {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, null,
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
	}

	private EventResponseDTO publishedEvent() {
		EventResponseDTO event = new EventResponseDTO();
		event.setId(1L);
		event.setName("Java Conference");
		event.setAvailableSeats(100);
		event.setTicketPrice(BigDecimal.valueOf(999));
		event.setStatus("PUBLISHED");
		return event;
	}

	private Booking booking() {
		return Booking.builder().id(1L).eventId(1L).userEmail("customer@gmail.com").numberOfTickets(2)
				.totalAmount(BigDecimal.valueOf(1998)).status(BookingStatus.CONFIRMED).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();
	}

	@Test
	void bookTicket_ShouldCreateBookingSuccessfully() {
		mockCustomerLogin("customer@gmail.com");

		BookingRequestDTO request = new BookingRequestDTO();
		request.setEventId(1L);
		request.setNumberOfTickets(2);

		EventResponseDTO event = publishedEvent();

		when(eventClient.getEventById(1L)).thenReturn(event);

		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
			Booking b = invocation.getArgument(0);
			b.setId(1L);
			b.setCreatedAt(LocalDateTime.now());
			b.setUpdatedAt(LocalDateTime.now());
			return b;
		});

		BookingResponseDTO response = bookingService.bookTicket(request);

		assertEquals(1L, response.getId());
		assertEquals(1L, response.getEventId());
		assertEquals("customer@gmail.com", response.getUserEmail());
		assertEquals(2, response.getNumberOfTickets());
		assertEquals(BigDecimal.valueOf(1998), response.getTotalAmount());
		assertEquals(BookingStatus.CONFIRMED, response.getStatus());

		verify(eventClient).reduceSeats(1L, 2);
		verify(bookingRepository).save(any(Booking.class));
		verify(bookingEventProducer).publishBookingCreated(any(BookingCreatedEvent.class));
	}

	@Test
	void bookTicket_ShouldThrowException_WhenEventNotFound() {
		mockCustomerLogin("customer@gmail.com");

		BookingRequestDTO request = new BookingRequestDTO();
		request.setEventId(1L);
		request.setNumberOfTickets(2);

		when(eventClient.getEventById(1L)).thenReturn(null);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.bookTicket(request));

		assertEquals("Event not found", ex.getMessage());

		verify(eventClient, never()).reduceSeats(anyLong(), anyInt());
		verify(bookingRepository, never()).save(any());
		verify(bookingEventProducer, never()).publishBookingCreated(any());
	}

	@Test
	void bookTicket_ShouldThrowException_WhenEventNotPublished() {
		mockCustomerLogin("customer@gmail.com");

		BookingRequestDTO request = new BookingRequestDTO();
		request.setEventId(1L);
		request.setNumberOfTickets(2);

		EventResponseDTO event = publishedEvent();
		event.setStatus("PROPOSED");

		when(eventClient.getEventById(1L)).thenReturn(event);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.bookTicket(request));

		assertEquals("Event is not available for booking", ex.getMessage());

		verify(eventClient, never()).reduceSeats(anyLong(), anyInt());
	}

	@Test
	void bookTicket_ShouldThrowException_WhenNotEnoughSeats() {
		mockCustomerLogin("customer@gmail.com");

		BookingRequestDTO request = new BookingRequestDTO();
		request.setEventId(1L);
		request.setNumberOfTickets(5);

		EventResponseDTO event = publishedEvent();
		event.setAvailableSeats(2);

		when(eventClient.getEventById(1L)).thenReturn(event);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.bookTicket(request));

		assertEquals("Not enough seats available", ex.getMessage());

		verify(eventClient, never()).reduceSeats(anyLong(), anyInt());
	}

	@Test
	void getMyBookings_ShouldReturnLoggedInUserBookings() {
		mockCustomerLogin("customer@gmail.com");

		when(bookingRepository.findByUserEmail("customer@gmail.com")).thenReturn(List.of(booking()));

		List<BookingResponseDTO> response = bookingService.getMyBookings();

		assertEquals(1, response.size());
		assertEquals("customer@gmail.com", response.get(0).getUserEmail());
		assertEquals(BookingStatus.CONFIRMED, response.get(0).getStatus());
	}

	@Test
	void getBookingById_ShouldReturnBooking_WhenOwnerAccesses() {
		mockCustomerLogin("customer@gmail.com");

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking()));

		BookingResponseDTO response = bookingService.getBookingById(1L);

		assertEquals(1L, response.getId());
		assertEquals("customer@gmail.com", response.getUserEmail());
	}

	@Test
	void getBookingById_ShouldReturnBooking_WhenAdminAccesses() {
		mockAdminLogin("admin@gmail.com");

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking()));

		BookingResponseDTO response = bookingService.getBookingById(1L);

		assertEquals(1L, response.getId());
		assertEquals("customer@gmail.com", response.getUserEmail());
	}

	@Test
	void getBookingById_ShouldThrowAccessDenied_WhenOtherCustomerAccesses() {
		mockCustomerLogin("other@gmail.com");

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking()));

		AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> bookingService.getBookingById(1L));

		assertEquals("You can view only your own booking", ex.getMessage());
	}

	@Test
	void getBookingById_ShouldThrowException_WhenBookingNotFound() {
		mockCustomerLogin("customer@gmail.com");

		when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

		RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.getBookingById(1L));

		assertEquals("Booking not found", ex.getMessage());
	}

	@Test
	void cancelBooking_ShouldCancelSuccessfully() {
		mockCustomerLogin("customer@gmail.com");

		Booking booking = booking();

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
		when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

		BookingResponseDTO response = bookingService.cancelBooking(1L);

		assertEquals(BookingStatus.CANCELLED, response.getStatus());

		verify(eventClient).increaseSeats(1L, 2);
		verify(bookingRepository).save(booking);
		verify(bookingEventProducer).publishBookingCancelled(any(BookingCancelledEvent.class));
	}

	@Test
	void cancelBooking_ShouldThrowAccessDenied_WhenOtherCustomerCancels() {
		mockCustomerLogin("other@gmail.com");

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking()));

		AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> bookingService.cancelBooking(1L));

		assertEquals("You can cancel only your own booking", ex.getMessage());

		verify(eventClient, never()).increaseSeats(anyLong(), anyInt());
		verify(bookingRepository, never()).save(any());
	}

	@Test
	void cancelBooking_ShouldThrowException_WhenAlreadyCancelled() {
		mockCustomerLogin("customer@gmail.com");

		Booking booking = booking();
		booking.setStatus(BookingStatus.CANCELLED);

		when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

		RuntimeException ex = assertThrows(RuntimeException.class, () -> bookingService.cancelBooking(1L));

		assertEquals("Booking already cancelled", ex.getMessage());

		verify(eventClient, never()).increaseSeats(anyLong(), anyInt());
		verify(bookingRepository, never()).save(any());
	}

	@Test
	void getAllBookings_ShouldReturnAllBookings() {
		when(bookingRepository.findAll()).thenReturn(List.of(booking()));

		List<BookingResponseDTO> response = bookingService.getAllBookings();

		assertEquals(1, response.size());
		assertEquals(BookingStatus.CONFIRMED, response.get(0).getStatus());
	}
}
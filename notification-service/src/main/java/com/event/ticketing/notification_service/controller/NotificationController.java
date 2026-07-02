//package com.event.ticketing.notification_service.controller;
//
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.event.ticketing.notification_service.NotificationService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/notifications")
//@RequiredArgsConstructor
//public class NotificationController {
//
//    private final NotificationService notificationService;
//
//    @PostMapping("/send")
//    public String sendNotification(@RequestParam String type,
//                                   @RequestParam String to,
//                                   @RequestParam String message) {
//
//        notificationService.sendNotification(type, to, message);
//        return "Notification sent using " + type;
//    }
//}
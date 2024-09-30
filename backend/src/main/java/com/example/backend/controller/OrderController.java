package com.example.backend.controller;

import com.example.backend.dto.OrderDTO;
import com.example.backend.entity.Order;
import com.example.backend.repositoty.CartRepository;
import com.example.backend.repositoty.OrderRepository;
import com.example.backend.service.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    OrderRepository orderRepository;

    @GetMapping("/api/orders")
    public ResponseEntity getOrder (
            @CookieValue(value = "token", required = false) String token
    ) {

        if (!jwtService.isValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        int memberId = jwtService.getId(token);
        List<Order> orders = orderRepository.findByMemberIdOrderByIdDesc(memberId);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/api/orders")
    public ResponseEntity pushOrder (
            @RequestBody OrderDTO orderDTO,
            @CookieValue(value = "token", required = false) String token
    ) {

        if(!jwtService.isValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        int memberId = jwtService.getId(token);
        Order order = new Order();

        order.setMemberId(memberId);
        order.setName(orderDTO.getName());
        order.setAddress(orderDTO.getAddress());
        order.setPayment(orderDTO.getPayment());
        order.setCardNumber(orderDTO.getCardNumber());
        order.setItems(orderDTO.getItems());

        orderRepository.save(order);
        cartRepository.deleteByMemberId(memberId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
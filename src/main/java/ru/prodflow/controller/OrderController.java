package ru.prodflow.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.prodflow.dto.UpdateOrderDto;
import ru.prodflow.dto.request.OrderRequestDto;
import ru.prodflow.dto.response.OrderResponseDto;
import ru.prodflow.enums.OrderStatus;
import ru.prodflow.mapper.OrderMapper;
import ru.prodflow.model.Order;
import ru.prodflow.model.User;
import ru.prodflow.service.interfaces.OrderService;
import ru.prodflow.service.interfaces.UserService;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final UserService userService;

    @GetMapping("/my/{id}")
    public OrderResponseDto getOrder(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();

        Order order = orderService.getOrderById(id);

        User user = userService.findUserByEmail(email);

        if (!Objects.equals(order.getCustomer().getId(), user.getId())) {
            throw new AccessDeniedException("Вам недоступен этот заказ.");
        }
        return OrderMapper.toResponseDto(order);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public List<OrderResponseDto> getOrders(Authentication authentication) {
        String email = authentication.getName();

        User user = userService.findUserByEmail(email);

        return orderService.findByCustomerId(user.getId()).stream().map(OrderMapper::toResponseDto).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public List<OrderResponseDto> getAllOrders() {
        return orderService.findAll().stream().map(OrderMapper::toResponseDto).toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping(params = "status")
    public List<OrderResponseDto> getOrdersByStatus(@RequestParam OrderStatus status) {
        return orderService.findAllByStatus(status).stream().map(OrderMapper::toResponseDto).toList();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{id}/cancel")
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/my/{id}/cancel")
    public void cancelOrderByCustomer(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();

        User user = userService.findUserByEmail(email);
        Order order = orderService.getOrderById(id);

        if (!Objects.equals(order.getCustomer().getId(), user.getId())) {
            throw new AccessDeniedException("Вам недоступен этот заказ.");
        }
        orderService.cancelOrder(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/my/{id}")
    public void updateOrderByCustomer(@PathVariable Long id,
                            @Valid @RequestBody UpdateOrderDto updateOrderDto,
                            Authentication authentication) {
        String email = authentication.getName();

        User user = userService.findUserByEmail(email);
        Order order = orderService.getOrderById(id);

        if (!Objects.equals(order.getCustomer().getId(), user.getId())) {
            throw new AccessDeniedException("Вам недоступен этот заказ.");
        }
        orderService.updateOrder(id, updateOrderDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{id}")
    public void updateOrder(@PathVariable Long id, @Valid @RequestBody UpdateOrderDto updateOrderDto) {
        orderService.updateOrder(id, updateOrderDto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public OrderResponseDto createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto, Authentication authentication) {
        String email = authentication.getName();

        User user = userService.findUserByEmail(email);

        return OrderMapper.toResponseDto(orderService.createOrder(orderRequestDto, user.getId()));
    }
}

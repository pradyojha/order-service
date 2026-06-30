package com.example.orderservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  // Injected from config (application.yml or an environment variable) — never hardcoded.
  @Value("${order.service.region}")
  private String region;

  @GetMapping("/{id}")
  public Order getOrder(@PathVariable String id) {
    return new Order(id, "CONFIRMED", region, LocalDate.now().toString());
  }

  // A Java record = a concise immutable data carrier; Spring serializes it to JSON automatically.
  public record Order(String id, String status, String region, String createdDate) {}
}
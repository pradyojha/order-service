package com.example.orderservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

  private final BlobService blobService;

  public OrderController(BlobService blobService) {
    this.blobService = blobService;
  }

  @PostMapping("/{id}/receipt")
  public String uploadReceipt(@PathVariable String id, @RequestBody String content) {
    validateOrderId(id);
    blobService.uploadReceipt(id, content);
    return "Receipt stored for order " + id;
  }

  @GetMapping("/{id}/receipt")
  public String getReceipt(@PathVariable String id) {
    validateOrderId(id);
    String receipt = blobService.downloadReceipt(id);
    return receipt != null ? receipt : "No receipt found for order " + id;
  }

  private void validateOrderId(String id) {
    if (!ORDER_ID_PATTERN.matcher(id).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order id");
    }
  }

  // Injected from config (application.yml or an environment variable) — never hardcoded.
  @Value("${order.service.region}")
  private String region;

  @GetMapping("/{id}")
  public Order getOrder(@PathVariable String id) {
    return new Order(id, "PLACED", region, LocalDate.now().toString());
  }

  // A Java record = a concise immutable data carrier; Spring serializes it to JSON automatically.
  public record Order(String id, String status, String region, String createdDate) {}
}
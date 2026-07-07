package com.example.orderservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OrderControllerTest {

  @Autowired
  private WebApplicationContext wac;

  @MockitoBean
  private BlobService blobService;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  void returnsOrderById() throws Exception {
    mockMvc.perform(get("/api/orders/42"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("42"))
        .andExpect(jsonPath("$.status").value("PLACED"));
  }

  @Test
  void uploadReceiptStoresContentAndReturnsConfirmation() throws Exception {
    mockMvc.perform(post("/api/orders/42/receipt")
            .contentType("text/plain")
            .content("Order 42 receipt"))
        .andExpect(status().isOk())
        .andExpect(content().string("Receipt stored for order 42"));

    verify(blobService).uploadReceipt("42", "Order 42 receipt");
  }

  @Test
  void getReceiptReturnsStoredContent() throws Exception {
    when(blobService.downloadReceipt("42")).thenReturn("Order 42 receipt");

    mockMvc.perform(get("/api/orders/42/receipt"))
        .andExpect(status().isOk())
        .andExpect(content().string("Order 42 receipt"));
  }

  @Test
  void getReceiptReturnsFallbackMessageWhenMissing() throws Exception {
    when(blobService.downloadReceipt("42")).thenReturn(null);

    mockMvc.perform(get("/api/orders/42/receipt"))
        .andExpect(status().isOk())
        .andExpect(content().string("No receipt found for order 42"));
  }

  @Test
  void uploadReceiptRejectsInvalidOrderId() throws Exception {
    mockMvc.perform(post("/api/orders/bad<script>id/receipt")
            .contentType("text/plain")
            .content("payload"))
        .andExpect(status().isBadRequest());

    verify(blobService, never()).uploadReceipt(anyString(), anyString());
  }

  @Test
  void getReceiptRejectsInvalidOrderId() throws Exception {
    mockMvc.perform(get("/api/orders/bad<script>id/receipt"))
        .andExpect(status().isBadRequest());
  }
}

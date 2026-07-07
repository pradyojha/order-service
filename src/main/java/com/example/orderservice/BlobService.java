package com.example.orderservice;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
public class BlobService {

  private final BlobContainerClient containerClient;

  @Autowired
  public BlobService(
      @Value("${azure.storage.account}") String accountName,
      @Value("${azure.storage.container}") String containerName) {
    // DefaultAzureCredential: uses your `az login` locally, Workload Identity on AKS.
    this(new BlobServiceClientBuilder()
        .endpoint("https://" + accountName + ".blob.core.windows.net")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient()
        .getBlobContainerClient(containerName));
  }

  BlobService(BlobContainerClient containerClient) {
    this.containerClient = containerClient;
  }

  public void uploadReceipt(String orderId, String content) {
    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    containerClient.getBlobClient("order-" + orderId + ".txt")
        .upload(new ByteArrayInputStream(bytes), bytes.length, true); // true = overwrite
  }

  public String downloadReceipt(String orderId) {
    try {
      return containerClient.getBlobClient("order-" + orderId + ".txt")
          .downloadContent().toString();
    } catch (BlobStorageException e) {
      if (e.getStatusCode() == 404) return null;
      throw e;
    }
  }
}
package com.example.orderservice;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlobServiceTest {

  private final BlobContainerClient containerClient = mock(BlobContainerClient.class);
  private final BlobClient blobClient = mock(BlobClient.class);
  private final BlobService blobService = new BlobService(containerClient);

  @Test
  void uploadReceiptWritesContentToBlob() {
    when(containerClient.getBlobClient("order-42.txt")).thenReturn(blobClient);

    blobService.uploadReceipt("42", "hello");

    verify(blobClient).upload(any(InputStream.class), eq(5L), eq(true));
  }

  @Test
  void downloadReceiptReturnsContentWhenBlobExists() {
    when(containerClient.getBlobClient("order-42.txt")).thenReturn(blobClient);
    when(blobClient.downloadContent()).thenReturn(BinaryData.fromString("receipt content"));

    String result = blobService.downloadReceipt("42");

    assertThat(result).isEqualTo("receipt content");
  }

  @Test
  void downloadReceiptReturnsNullWhenBlobMissing() {
    when(containerClient.getBlobClient("order-42.txt")).thenReturn(blobClient);
    BlobStorageException notFound = mock(BlobStorageException.class);
    when(notFound.getStatusCode()).thenReturn(404);
    when(blobClient.downloadContent()).thenThrow(notFound);

    String result = blobService.downloadReceipt("42");

    assertThat(result).isNull();
  }

  @Test
  void downloadReceiptRethrowsNonNotFoundErrors() {
    when(containerClient.getBlobClient("order-42.txt")).thenReturn(blobClient);
    BlobStorageException serverError = mock(BlobStorageException.class);
    when(serverError.getStatusCode()).thenReturn(500);
    when(blobClient.downloadContent()).thenThrow(serverError);

    assertThatThrownBy(() -> blobService.downloadReceipt("42")).isSameAs(serverError);
  }
}

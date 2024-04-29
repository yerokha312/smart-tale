package dev.yerokha.smarttale.dto;

public record PurchaseRequest(
        String title,
        String description,
        String price,
        String requesterEmail,
        String requesterPhoneNumber
) {
}
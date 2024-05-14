package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

// for inner usage
public record PurchaseRequest(
        @NotNull
        String title,
        @NotNull
        String description,
        @NotNull
        String price,
        @NotNull
        String buyerEmail,
        @NotNull
        String buyerPhoneNumber,
        @NotNull
        String sellerEmail,
        @NotNull
        String sellerPhoneNumber
) {
}
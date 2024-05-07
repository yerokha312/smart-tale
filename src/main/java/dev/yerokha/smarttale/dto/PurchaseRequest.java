package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

public record PurchaseRequest(
        @NotNull
        String title,
        @NotNull
        String description,
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
package dev.yerokha.smarttale.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.DEDUCTION
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateJobRequest.class),
        @JsonSubTypes.Type(value = CreateOrderRequest.class),
        @JsonSubTypes.Type(value = CreateProductRequest.class)
})
public interface CreateAdInterface {
}

package dev.yerokha.smarttale.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateJobRequest.class, name = "job"),
        @JsonSubTypes.Type(value = CreateOrderRequest.class, name = "order"),
        @JsonSubTypes.Type(value = CreateProductRequest.class, name = "product")
})
public interface CreateAdInterface {
}

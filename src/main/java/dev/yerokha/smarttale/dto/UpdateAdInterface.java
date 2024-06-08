package dev.yerokha.smarttale.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UpdateJobRequest.class, name = "job"),
        @JsonSubTypes.Type(value = UpdateOrderRequest.class, name = "order"),
        @JsonSubTypes.Type(value = UpdateProductRequest.class, name = "product")
})
public interface UpdateAdInterface {
}

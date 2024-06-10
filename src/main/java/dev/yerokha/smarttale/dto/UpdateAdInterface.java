package dev.yerokha.smarttale.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UpdateJobRequest.class),
        @JsonSubTypes.Type(value = UpdateOrderRequest.class),
        @JsonSubTypes.Type(value = UpdateProductRequest.class)
})
public interface UpdateAdInterface {
}

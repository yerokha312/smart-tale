package dev.yerokha.smarttale.mapper;

import dev.yerokha.smarttale.dto.CustomPage;
import org.springframework.data.domain.Page;

public class CustomPageMapper {
    public static <T> CustomPage<T> getCustomPage(Page<T> page) {
        return new CustomPage<>(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.isEmpty()
        );
    }
}

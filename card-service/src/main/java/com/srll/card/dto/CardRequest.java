package com.srll.card.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CardRequest {

    @NotBlank
    private String front;

    @NotBlank
    private String back;
}

package com.srll.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeckRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    @NotBlank
    @Size(max = 50)
    private String language;
}

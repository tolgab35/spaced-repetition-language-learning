package com.srll.card.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ReviewRequest {

    @Min(0) @Max(5)
    private int rating;
}

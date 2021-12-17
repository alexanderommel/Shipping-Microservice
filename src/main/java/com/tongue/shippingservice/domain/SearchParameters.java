package com.tongue.shippingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchParameters {
    private Float initial_radius;
    private Float increase_ratio;
    private int max_rounds;
    private int max_stack_size;
    private SearchFilter searchFilter;
    private StackingMethod stackingMethod;

    public enum SearchFilter{
        BASIC_CIRCULAR
    }

    public enum StackingMethod{
        BASIC_EUCLIDEAN,NO_STACKING
    }
}

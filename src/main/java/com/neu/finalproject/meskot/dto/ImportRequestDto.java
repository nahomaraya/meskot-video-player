package com.neu.finalproject.meskot.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // Import this
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor // Jackson needs an empty constructor
@AllArgsConstructor
public class ImportRequestDto {

    @JsonProperty("itemIdentifier") // Forces Jackson to look for this exact key
    private String itemIdentifier;

    @JsonProperty("uploaderId")
    private Long uploaderId;
}
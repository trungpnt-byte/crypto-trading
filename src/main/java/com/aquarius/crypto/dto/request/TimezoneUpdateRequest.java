package com.aquarius.crypto.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TimezoneUpdateRequest {
    private String timezone;
}

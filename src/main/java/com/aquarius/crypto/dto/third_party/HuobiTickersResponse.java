package com.aquarius.crypto.dto.third_party;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class HuobiTickersResponse {
    private String status;
    private List<HuobiTicker> data;
}
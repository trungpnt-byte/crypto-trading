package com.aquarius.crypto.dto;

import com.aquarius.crypto.dto.third_party.HuobiTicker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HuobiTickersWrapper {
    private Collection<HuobiTicker> data;
}

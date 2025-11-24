package com.aquarius.crypto.dto.third_party;

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

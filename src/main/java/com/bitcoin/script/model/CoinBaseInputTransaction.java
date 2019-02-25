package com.bitcoin.script.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoinBaseInputTransaction{
    String coinbase;
    Long sequence;
    int outputNum;
    String witness;
}

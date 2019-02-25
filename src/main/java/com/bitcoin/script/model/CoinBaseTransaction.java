package com.bitcoin.script.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CoinBaseTransaction extends Transaction{
    int version;
    int inputCount;
    CoinBaseInputTransaction vin;
    int outputCount;
    List<OutputTransaction> vout;
    Long lockTime;
}

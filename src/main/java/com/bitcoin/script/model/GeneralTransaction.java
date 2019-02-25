package com.bitcoin.script.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralTransaction extends Transaction{
    int version;
    int inputCount;
    List<InputTransaction> vin;
    int outputCount;
    List<OutputTransaction> vout;
    Long lockTime;

}

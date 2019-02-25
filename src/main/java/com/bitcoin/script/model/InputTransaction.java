package com.bitcoin.script.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputTransaction {
    String txHash;
    int outputNo;
    ScriptSig scriptSig;
    Long sequence;

}

package com.bitcoin.script.model;

import lombok.Data;

@Data
public class OutputTransaction {
    Long value;
    int outputNo;
    ScriptPubKey scriptPubKey;

    public OutputTransaction(Long value, int outputNo, ScriptPubKey scriptPubKey) {
        this.value = value;
        this.outputNo = outputNo;
        this.scriptPubKey = scriptPubKey;
    }
}

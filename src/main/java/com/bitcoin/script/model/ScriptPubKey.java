package com.bitcoin.script.model;

import lombok.Data;

@Data
public class ScriptPubKey {
    String hex;
    String asm;

    public ScriptPubKey(String hex, String asm) {
        this.hex = hex;
        this.asm = asm;
    }
}

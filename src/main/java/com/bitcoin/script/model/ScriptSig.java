package com.bitcoin.script.model;

import lombok.Data;

@Data
public class ScriptSig {
    String hex;
    String asm;
    String witness;

    public ScriptSig(){

    }

    public ScriptSig(String hex, String asm, String witness) {
        this.hex = hex;
        this.asm = asm;
        this.witness = witness;
    }
}

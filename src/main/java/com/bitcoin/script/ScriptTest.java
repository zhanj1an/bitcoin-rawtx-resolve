package com.bitcoin.script;

import com.alibaba.fastjson.JSONObject;
import com.bitcoin.script.model.*;
import org.bitcoinj.script.ScriptOpCodes;

import java.util.ArrayList;
import java.util.List;

public class ScriptTest {

    public static void main(String[] args) {
        String transactionHex = "01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff25030e640304000018c3124d696e656420627920425443204775696c640800021855cc010000ffffffff01a02b7d98000000001976a91427a1f12771de5cc3b73941664b2537c15316be4388ac00000000";

        String json = JSONObject.toJSONString(getTransaction(transactionHex));
        System.out.println(json);
    }
    
    private static Object getTransaction(String transactionHex){
        int index = 0;
        int version = Integer.parseInt(LittleEndianToBigEndian(transactionHex.substring(0, index += 8)), 16);
        //是否为隔离见证
        boolean isSegwit = false;
        //是否为coinbase交易
        boolean isCoinBase = false;
        int inputCount = Integer.parseInt(transactionHex.substring(index, index += 2),16);
        //读取到00表明字段为 0001 为隔离见证
        if(inputCount == 0){
            isSegwit = true;
            inputCount = Integer.parseInt(transactionHex.substring(index += 2, index += 2),16);
        }
        List<InputTransaction> vin = new ArrayList<InputTransaction>(inputCount);
        CoinBaseInputTransaction coinBaseInputTransaction = null;
        for (int i = 0; i < inputCount; i++) {
            InputTransaction inputTransaction;
            ScriptSig scriptSig = new ScriptSig();
            //获得输入所在txid
            String txHash = LittleEndianToBigEndian(transactionHex.substring(index, index += 64));
            //表明为coinbase交易
            if(txHash.split("0").length == 0){
                isCoinBase = true;
            }
            //普通交易封装
            if(!isCoinBase) {
                int outputNo = Integer.parseInt(LittleEndianToBigEndian(transactionHex.substring(index, index += 8)), 16);
                int scriptSigLength;
                //获得下次读取字节数
                if (transactionHex.substring(index, index + 2).equals("fd")) {
                    scriptSigLength = Integer.parseInt(LittleEndianToBigEndian(transactionHex.substring(index + 2, index += 6)), 16) * 2;
                } else if (transactionHex.substring(index, index + 2).equals("fe")) {
                    scriptSigLength = Integer.parseInt(LittleEndianToBigEndian(transactionHex.substring(index + 2, index += 10)), 16) * 2;
                } else if (transactionHex.substring(index, index + 2).equals("ff")) {
                    scriptSigLength = Integer.parseInt(LittleEndianToBigEndian(transactionHex.substring(index + 2, index += 18)), 16) * 2;
                } else {
                    scriptSigLength = Integer.parseInt(transactionHex.substring(index, index += 2), 16) * 2;
                }
                String scriptSigHex = transactionHex.substring(index, index += scriptSigLength);
                String scriptSigAsm = getScriptSigAsm(scriptSigHex);
                scriptSig.setHex(scriptSigHex);
                scriptSig.setAsm(scriptSigAsm);
                Long sequence = Long.parseLong(LittleEndianToBigEndian(transactionHex.substring(index, index += 8)), 16);
                inputTransaction = new InputTransaction(txHash, outputNo, scriptSig, sequence);
                vin.add(inputTransaction);
            }else{
                int scriptSigLength = Integer.parseInt(transactionHex.substring(index += 8, index += 2), 16) * 2;
                String coinbase = transactionHex.substring(index, index += scriptSigLength);
                Long sequence = Long.parseLong(LittleEndianToBigEndian(transactionHex.substring(index, index += 8)), 16);
                coinBaseInputTransaction = new CoinBaseInputTransaction(coinbase, sequence, 0, getCoinBaseWitness());
            }
        }
        int outputCount = Integer.parseInt(transactionHex.substring(index, index += 2),16);
        List<OutputTransaction> vout = new ArrayList<OutputTransaction>();
        for (int i = 0; i < outputCount; i++) {
            Long value = Long.parseLong(LittleEndianToBigEndian(transactionHex.substring(index, index += 16)), 16);
            int scriptPubKeyLength = Integer.parseInt(transactionHex.substring(index, index += 2), 16) * 2;
            String hex = transactionHex.substring(index, index += scriptPubKeyLength);
            String asm;
            asm = getScriptPubKeyAsm(hex);
            ScriptPubKey scriptPubKey = new ScriptPubKey(hex, asm);
            OutputTransaction outputTransaction = new OutputTransaction(value, i, scriptPubKey);
            vout.add(outputTransaction);
        }

        //读取witness信息
        if(isSegwit){
            String witness = transactionHex.substring(index, transactionHex.length() - 8);
            index = transactionHex.length() - 8;
            Long lockTime = Long.parseLong(LittleEndianToBigEndian(transactionHex.substring(index)),16);
            if(isCoinBase){
                return new CoinBaseTransaction(version, inputCount, coinBaseInputTransaction, outputCount, vout, lockTime);
            }
            return new GeneralTransaction(version, inputCount, getWitness(vin, witness), outputCount, vout, lockTime);
        }

        Long lockTime = Long.parseLong(LittleEndianToBigEndian(transactionHex.substring(index)),16);

        if(isCoinBase){
            return new CoinBaseTransaction(version, 1, coinBaseInputTransaction, outputCount, vout, lockTime);
        }
        return new GeneralTransaction(version, inputCount, vin, outputCount, vout, lockTime);
    }

    /**
     * 从输入脚本中提取vout asm信息
     * @param scriptPubKeyHex hex字段
     * @return asm字段
     */
    private static String getScriptPubKeyAsm(String scriptPubKeyHex){
        StringBuilder asm = new StringBuilder();
        int index = 0;
        while(index < scriptPubKeyHex.length()) {
            try {
                int value = Integer.parseInt(scriptPubKeyHex.substring(index, index += 2), 16);
                String opCodeName = ScriptOpCodes.getOpCodeName(value);
                if (("NON_OP(" + value + ")").equals(opCodeName)) {
                   String s = scriptPubKeyHex.substring(index, index += (value * 2));
                   asm.append(s).append(" ");
                } else {
                    asm.append("OP_").append(opCodeName).append(" ");
                }
            }catch (StringIndexOutOfBoundsException e){
                asm.append("[error] ");
            }
        }
        return asm.substring(0, asm.length() - 1);
    }

    /**
     * 从输入脚本中提取vin asm信息
     * @param scriptSigHex hex字段
     * @return asm字段
     */
    private static String getScriptSigAsm(String scriptSigHex){
        StringBuilder asm = new StringBuilder();
        int index = 0;
        while(index < scriptSigHex.length()) {
            int value = Integer.parseInt(scriptSigHex.substring(index, index += 2), 16);
            String opCodeName = ScriptOpCodes.getOpCodeName(value);
            //当前读取的字节不在脚本操作符map中时 字节为读取的长度
            if(("NON_OP(" + value + ")").equals(opCodeName)){
                String sigScript = scriptSigHex.substring(index, index += (value * 2));
                if("30".equals(sigScript.substring(0, 2))){
                    String sig = sigScript.substring(0, sigScript.length() - 2);
                    asm.append(sig);
                    switch (Integer.parseInt(sigScript.substring(sigScript.length() - 2))){
                        case 1:
                            asm.append("[ALL]");
                            break;
                        case 2:
                            asm.append("[NONE]");
                            break;
                        case 3:
                            asm.append("[SINGLE]");
                            break;
                    }
                }
                else{
                    asm.append(sigScript);
                }
            }
            //读取字节为00 对应 OP_0
            else if("0".equals(opCodeName)){
                asm.append("0" + " ");
            }
            //当读取到的字节为4c 4d 4e时 得到下一次读取的字节长度
            else if("PUSHDATA".equals(opCodeName.substring(0, opCodeName.length() - 1))){
                //入栈字节数
                int pushDataNumber = Integer.parseInt(opCodeName.substring(opCodeName.length() - 1));
                int length = Integer.parseInt(LittleEndianToBigEndian(scriptSigHex.substring(index, index += pushDataNumber * 2)), 16);
                String s = scriptSigHex.substring(index, index += (length * 2));
                asm.append(s);
            }
        }
        return asm.toString();
    }

    /**
     * 地址转换
     * @param littleEndian littleEndian类型地址
     * @return BigEndian类型正确地址
     */
    private static String LittleEndianToBigEndian(String littleEndian){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < littleEndian.length() - 2; i+=2) {
            sb.insert(0,littleEndian.substring(i,i+2));
        }
        sb.insert(0,littleEndian.substring(littleEndian.length() - 2));
        return sb.toString();
    }

    /**
     * 从交易中提取隔离见证信息
     * @param vin 输入交易集合
     * @param witness 隔离见证原始信息
     * @return 封装隔离见证信息后的输入交易集合
     */
    private static List<InputTransaction> getWitness(List<InputTransaction> vin, String witness){
        int index = 0;
        for (InputTransaction inputTransaction : vin){
            int sigCount = Integer.parseInt(witness.substring(index, index += 2), 16);
            StringBuilder scriptSig = new StringBuilder();
            for (int j = 0; j < sigCount; j++) {
                int sigLength = Integer.parseInt(witness.substring(index, index += 2), 16) * 2;
                String str = witness.substring(index, index += sigLength);
                scriptSig.append(str);
            }
            inputTransaction.getScriptSig().setWitness(scriptSig.toString());
        }
        return vin;
    }

    /**
     * 获取coinbase隔离见证信息
     * @return 固定值
     */
    private static String getCoinBaseWitness(){
        return "0000000000000000000000000000000000000000000000000000000000000000";
    }
}

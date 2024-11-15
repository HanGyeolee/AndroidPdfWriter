package com.hangyeolee.androidpdfwriter.binary;

class XRef {
    byte[] pos;
    byte[] gen;
    byte fn;

    public XRef(long pos, byte fn){
        this(pos, 0, fn);
    }
    public XRef(long pos, int gen, byte fn){
        this.pos = Pos2Ten(pos);
        this.gen = Gen2Fiv(gen);
        this.fn = fn;
    }

    private byte[] Pos2Ten(long pos){
        byte[] result = new byte[10];
        byte[] tmp = String.valueOf(pos).getBytes(BinaryObjectManager.US_ASCII);

        int i = result.length - 1;
        int j = tmp.length - 1;

        for(; i >= 0 ; i--,j--){
            if(j >= 0){
                result[i] = tmp[j];
            }else{
                result[i] = '0';
            }
        }

        return result;
    }
    private byte[] Gen2Fiv(int gen){
        byte[] result = new byte[5];
        byte[] tmp = String.valueOf(gen).getBytes(BinaryObjectManager.US_ASCII);

        int i = result.length - 1;
        int j = tmp.length - 1;

        for(; i >= 0 ; i--,j--){
            if(j >= 0){
                result[i] = tmp[j];
            }else{
                result[i] = '0';
            }
        }

        return result;
    }

    public byte[] write(){
        byte[] result = new byte[pos.length + gen.length + 1 + 3];
        int i = 0;
        for(int j = 0; j < pos.length; j++, i++){
            result[i] = pos[j];
        }
        result[i++] = ' ';
        for(int j = 0; j < gen.length; j++, i++){
            result[i] = gen[j];
        }
        result[i++] = ' ';
        result[i++] = fn;
        result[i] = '\n';

        return result;
    }
}

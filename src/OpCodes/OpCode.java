package OpCodes;

import BytecodeUtils.BytecodeParser;

import java.util.Objects;

public class OpCode {
    public enum Type {
        INCLUDE((byte) 0x02);

        private byte code;

        Type(byte code) {
            this.code = code;
        }

        public byte getCode() {
            return code;
        }

        public static Type forBytecode(byte code) {
            for (Type type: values()) if (type.getCode() == code) return type;
            return null;
        }
    }

    private Type type;
    private byte[] arg;

    public OpCode(Type type, byte[] arg) {
        Objects.requireNonNull(this.type = type, "Type should not be null");
        Objects.requireNonNull(this.arg = arg, "Argument should not be null");
    }

    public Type getType() {
        return type;
    }

    public byte[] getArg() {
        return arg;
    }

    public byte[] toBytecode() {
        byte[] encodedArg = BytecodeParser.encodeValue(arg);
        byte[] result = new byte[encodedArg.length + 1];
        result[0] = type.getCode();
        System.arraycopy(encodedArg, 0, result, 1, encodedArg.length);
        return result;
    }
}

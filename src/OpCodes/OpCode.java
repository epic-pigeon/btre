package OpCodes;

import BytecodeUtils.BytecodeParser;

import java.util.Objects;

public class OpCode {
    public enum Type {
        INCLUDE((byte) 0x02), COPY((byte) 0x03),
        INVOKE_VIRTUAL((byte) 0x04), CAST((byte) 0x05),
        INVOKE_STATIC((byte) 0x06), STORE((byte) 0x06),
        LOAD((byte) 0x07), INT_CONST((byte) 0x08),
        STRING_CONST((byte) 0x09);

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
    private byte[][] args;

    public OpCode(Type type, byte[][] arg) {
        Objects.requireNonNull(this.type = type, "Type should not be null");
        Objects.requireNonNull(this.args = arg, "Argument should not be null");
    }

    public Type getType() {
        return type;
    }

    public byte[][] getArgs() {
        return args;
    }

    public byte[] toBytecode() {
        byte[] encodedArg = BytecodeParser.buildBytecodeArgs(args);
        byte[] result = new byte[encodedArg.length + 1];
        result[0] = type.getCode();
        System.arraycopy(encodedArg, 0, result, 1, encodedArg.length);
        return result;
    }
}

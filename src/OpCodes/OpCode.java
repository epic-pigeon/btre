package OpCodes;

import BytecodeUtils.BytecodeParser;
import BytecodeUtils.OpCodeExecutor;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class OpCode {
    public static class IncludeOpCode {
        public static String describe(byte[][] args) {
            return "path = \"" + OpCodeExecutor.stringFromBytes(args[0]) + "\"";
        }
        public static OpCode build(String filename) {
            return new OpCode(Type.INCLUDE, new byte[][]{
                    OpCodeExecutor.bytesFromString(filename)
            });
        }
    }
    public static class GotoOpCode {
        public static String describe(byte[][] args) {
            return "" + OpCodeExecutor.intFromBytes(args[0]);
        }
        public static OpCode build(Integer label) {
            return new OpCode(Type.GOTO, new byte[][]{
                OpCodeExecutor.bytesFromUnsignedInt(label)
            });
        }
    }
    public static class LabelOpCode {
        public static String describe(byte[][] args) {
            return "" + OpCodeExecutor.intFromBytes(args[0]);
        }
        public static OpCode build(Integer label) {
            return new OpCode(Type.LABEL, new byte[][]{
                    OpCodeExecutor.bytesFromUnsignedInt(label)
            });
        }
    }
    public static class QuackOpCode {
        public static String describe(byte[][] args) {
            return "just quack";
        }
        public static OpCode build() {
            return new OpCode(Type.PRINT_QUACK, new byte[][]{});
        }
    }
    public enum Type {
        INCLUDE((byte) 0x02, IncludeOpCode.class), COPY((byte) 0x03),
        INVOKE_VIRTUAL((byte) 0x04), CAST((byte) 0x05),
        INVOKE_STATIC((byte) 0x06), STORE((byte) 0x06),
        LOAD((byte) 0x07), INT_CONST((byte) 0x08),
        STRING_CONST((byte) 0x09), GOTO((byte) 0x10, GotoOpCode.class),
        LABEL((byte) 0x11, LabelOpCode.class), CLASS_DEF((byte) 0x12),
        METHOD_DEF((byte) 0x13), PRINT_QUACK((byte) 0x14, QuackOpCode.class),
        RETURN((byte) 0x15);

        private byte code;
        private Class opcodeClass;

        Type(byte code) {
            this.code = code;
        }

        Type(byte code, Class opcodeClass) {
            this.code = code;
            this.opcodeClass = opcodeClass;
        }

        public byte getCode() {
            return code;
        }

        public static Type forBytecode(byte code) {
            for (Type type: values()) if (type.getCode() == code) return type;
            return null;
        }

        public Class getOpcodeClass() {
            return opcodeClass;
        }
    }

    private Type type;
    private byte[][] args;

    public OpCode(Type type, byte[][] arg) {
        Objects.requireNonNull(this.type = type, "Type should not be null");
        Objects.requireNonNull(this.args = arg, "Argument should not be null");
    }

    public String describe() {
        String result = "Opcode " + type.name();
        try {
            Class opcodeClass = type.opcodeClass;
            Method method = opcodeClass.getMethod("describe", byte[][].class);
            String res = (String) method.invoke(null, (Object) args);
            result += ": " + res;
        } catch (Throwable e) {
            //e.printStackTrace();
        }
        return result;
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

    @Override
    public String toString() {
        return describe();
    }
}

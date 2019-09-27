package BytecodeUtils;

import OpCodes.OpCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BytecodeParser {
    public static final byte ARGUMENT_SEPARATOR = 0x00;
    public static final byte OPCODE_SEPARATOR = 0x01;

    public static byte[] encodeValue(byte[] value) {
        byte[] result = Arrays.copyOf(value, value.length);
        for (int i = 0; i < result.length; i++) {
            if (result[i] == OPCODE_SEPARATOR || result[i] == ARGUMENT_SEPARATOR) {
                result = Arrays.copyOf(result, result.length + 1);
                System.arraycopy(
                        result,
                        i,
                        result,
                        i + 1,
                        result.length - i - 1
                );
                i++;
            }
        }
        return result;
    }

    public static byte[] buildBytecodeArgs(byte[][] args) {
        ArrayList<Byte> result = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (i != 0) result.add(ARGUMENT_SEPARATOR);
            for (byte aByte: args[i]) {
                result.add(aByte);
            }
        }
        byte[] ret = new byte[result.size()];
        for (int i = 0; i < result.size(); i++) ret[i] = result.get(i);
        return ret;
    }

    public static byte[][] decodeValue(byte[] code) {
        ArrayList<ArrayList<Byte>> result = new ArrayList<ArrayList<Byte>>() {{
            add(new ArrayList<>());
        }};
        int currentArg = 0;
        for (int i = 0; i < code.length; i++) {
            if (code[i] == OPCODE_SEPARATOR) {
                i++;
                if (code[i] == OPCODE_SEPARATOR) {
                    result.get(currentArg).add(OPCODE_SEPARATOR);
                } else {
                    byte[][] ret = new byte[result.size()][];
                    for (int j = 0; j < result.size(); j++) {
                        ret[j] = new byte[result.get(j).size()];
                        for (int j1 = 0; j1 < result.get(j).size(); j1++) {
                            ret[j][j1] = result.get(j).get(j1);
                        }
                    }
                    return ret;
                }
            } else if (code[i] == ARGUMENT_SEPARATOR) {
                i++;
                if (code[i] == ARGUMENT_SEPARATOR) {
                    result.get(currentArg).add(ARGUMENT_SEPARATOR);
                } else {
                    currentArg++;
                    result.add(new ArrayList<>());
                }
            } else result.get(currentArg).add(code[i]);
        }
        byte[][] ret = new byte[result.size()][];
        for (int j = 0; j < result.size(); j++) {
            ret[j] = new byte[result.get(j).size()];
            for (int j1 = 0; j1 < result.get(j).size(); j1++) {
                ret[j][j1] = result.get(j).get(j1);
            }
        }
        return ret;
    }

    public static byte[] cutUntilSeparator(byte[] code) {
        int i;
        for (i = 0; i < code.length; i++) {
            if (code[i] == OPCODE_SEPARATOR) {
                i++;
                if (code[i] != OPCODE_SEPARATOR) break;
            }
        }
        return Arrays.copyOfRange(code, i, code.length);
    }

    public static List<OpCode> parseBytecode(byte[] code) {
        ArrayList<OpCode> result = new ArrayList<>();
        while (code.length > 0) {
            byte opCode = code[0];
            code = Arrays.copyOfRange(code, 1, code.length);
            byte[][] args = decodeValue(code);
            code = cutUntilSeparator(code);
            result.add(
                    new OpCode(
                            Objects.requireNonNull(OpCode.Type.forBytecode(opCode), "Unrecognized bytecode value " + opCode),
                            args
                    )
            );
        }
        return result;
    }

    public static byte[] buildBytecode(List<OpCode> opCodes) {
        ArrayList<Byte> result = new ArrayList<>();
        for (OpCode opCode: opCodes) {
            for (byte aByte: opCode.toBytecode()) result.add(aByte);
            result.add(OPCODE_SEPARATOR);
        }
        byte[] ret = new byte[result.size()];
        for (int j = 0; j < result.size(); j++) ret[j] = result.get(j);
        return ret;
    }
}

import BytecodeUtils.BytecodeParser;
import BytecodeUtils.OpCodeExecutor;
import FileUtils.FileManager;
import OpCodes.OpCode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Test {
    public static void main(String[] args) throws IOException {
        compileFile();
        runFile();
        BytecodeParser.describeBytecode(
                FileManager.readFile(new File("my_main"))
        );
    }

    private static void compileFile() throws IOException {
        FileManager.writeFile(new File("my_main"), BytecodeParser.buildBytecode(
                new ArrayList<OpCode>(){{
                    add(new OpCode(OpCode.Type.INCLUDE, new byte[][]{
                            new File("my_lib").getAbsolutePath().getBytes(StandardCharsets.UTF_8)
                    }));
                    add(new OpCode(OpCode.Type.PRINT_QUACK, new byte[][]{}));
                    add(new OpCode(OpCode.Type.GOTO, new byte[][]{new byte[]{0}}));
                    add(new OpCode(OpCode.Type.PRINT_QUACK, new byte[][]{}));
                    add(new OpCode(OpCode.Type.LABEL, new byte[][]{new byte[]{0}}));
                    add(new OpCode(OpCode.Type.PRINT_QUACK, new byte[][]{}));
                }}
        ));
    }

    private static void runFile() throws IOException {
        OpCodeExecutor.executeProgram(
                FileManager.readFile(new File("my_main")),
                new OpCodeExecutor.Environment(new ArrayList<>(), new HashMap<>(), new HashMap<>())
        );
    }
}

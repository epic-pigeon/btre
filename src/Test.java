import BytecodeUtils.BytecodeParser;
import BytecodeUtils.OpCodeExecutor;
import FileUtils.FileManager;
import OpCodes.OpCode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Test {
    public static void main(String[] args) throws IOException {
        compileFile();
        runFile();
        System.out.println("my_main:");
        BytecodeParser.describeBytecode(FileManager.readFile(new File("my_main")));
        System.out.println("my_lib:");
        BytecodeParser.describeBytecode(FileManager.readFile(new File("my_lib")));
    }

    private static void compileFile() throws IOException {
        FileManager.writeFile(new File("my_main"), BytecodeParser.buildBytecode(
                new ArrayList<OpCode>(){{
                    add(OpCode.IncludeOpCode.build("my_lib"));
                    add(OpCode.QuackOpCode.build());
                    add(OpCode.GotoOpCode.build(0));
                    add(OpCode.QuackOpCode.build());
                    add(OpCode.LabelOpCode.build(0));
                    add(OpCode.QuackOpCode.build());
                }}
        ));
    }

    private static void runFile() throws IOException {
        OpCodeExecutor.executeProgram(
                FileManager.readFile(new File("my_main")),
                new OpCodeExecutor.Environment(new HashMap<>(), new HashMap<>())
        );
    }
}

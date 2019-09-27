package BytecodeUtils;

import FileUtils.FileManager;
import OpCodes.OpCode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class OpCodeExecutor {
    public static final Charset defaultCharset = StandardCharsets.UTF_8;

    private static List<File> filePath = null;

    public static List<File> getFilePath() {
        return filePath;
    }

    public static void setFilePath(List<File> filePath) {
        OpCodeExecutor.filePath = filePath;
    }

    public static class Environment {
        private Map<String, BTREClass> classes = new HashMap<>();
        private Map<String, Environment> environments = new HashMap<>();

        public Map<String, Environment> getEnvironments() {
            return environments;
        }

        public void setEnvironments(Map<String, Environment> environments) {
            this.environments = environments;
        }

        public Map<String, BTREClass> getClasses() {
            return classes;
        }

        public void setClasses(Map<String, BTREClass> classes) {
            this.classes = classes;
        }

        public Environment(Map<String, BTREClass> classes, Map<String, Environment> environments) {
            this.classes = classes;
            this.environments = environments;
        }
    }

    public static class BTREObject {
        private BTREClass btreClass;
        private Map<String, BTREObject> properties;


    }

    public static Environment executeProgram(byte[] code, Environment environment) {
        return (Environment) runBytecode(code, environment, null, (BTREObject[]) null);
    }

    public static BTREObject executeFunction(byte[] code, Environment environment, BTREObject self, BTREObject... variables) {
        return (BTREObject) runBytecode(code, environment, self, variables);
    }

    public static void checkParams(byte[][] args, String name, int... possibleLengths) {
        for (int l : possibleLengths) if (args.length == l) return;
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < possibleLengths.length; i++) {
            if (i != 0) {
                if (i == possibleLengths.length - 1) {
                    string.append(" or ");
                } else {
                    string.append(", ");
                }
            }
            string.append(possibleLengths[i]);
        }
        throw new RuntimeException("Opcode " + name + " can only have " + string + " arguments (" + args.length + " got)");
    }

    public static String stringFromBytes(byte[] bytes) {
        return new String(bytes, defaultCharset);
    }

    public static byte[] bytesFromString(String string) {
        return string.getBytes(defaultCharset);
    }

    public static int intFromBytes(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result += bytes[i] * Math.pow(256, i);
        }
        return result;
    }

    public static byte[] bytesFromUnsignedInt(int num) {
        ArrayList<Byte> result = new ArrayList<>();
        while (num > 0) {
            result.add((byte) (num % 256));
            num /= 256;
        }
        if (result.size() == 0) result.add((byte) 0);
        byte[] ret = new byte[result.size()];
        for (int i = 0; i < result.size(); i++) {
            ret[i] = result.get(i);
        }
        return ret;
    }

    private static Object runBytecode(byte[] code, Environment environment, BTREObject self, BTREObject... args) {
        boolean isFunction = self != null && args != null;
        Environment env = isFunction ? new Environment(
                environment.classes,
                environment.environments
        ) : environment;
        BTREObject ret = null;
        List<BTREObject> variables = null;
        if (isFunction) {
            variables = new ArrayList<BTREObject>() {{
                add(self);
                addAll(Arrays.asList(args));
            }};
        }
        List<OpCode> opCodes = BytecodeParser.parseBytecode(code);
        Stack<BTREObject> stack = new Stack<>();
        Supplier<BTREObject> stackAccessor = () -> {
            if (stack.empty()) {
                throw new RuntimeException("Cannot fetch from stack");
            } else {
                return stack.pop();
            }
        };
        Map<Integer, Integer> labels = new HashMap<>();
        kar: for (int i = 0; i < opCodes.size(); i++) {
            OpCode opCode = opCodes.get(i);
            switch (opCode.getType()) {
                case INCLUDE: {
                    checkParams(opCode.getArgs(), "INCLUDE", 1);
                    String path = stringFromBytes(opCode.getArgs()[0]);
                    File file = FileManager.getFile(filePath, path);
                    if (file == null) throw new RuntimeException("Error: \"" + path + "\" is not in the file path");
                    byte[] bytes;
                    try {
                        bytes = FileManager.readFile(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    executeProgram(bytes, environment);
                } break;
                case LABEL: {
                    checkParams(opCode.getArgs(), "LABEL", 1);
                    int number = intFromBytes(opCode.getArgs()[0]);
                    labels.put(number, i);
                } break;
                case GOTO: {
                    checkParams(opCode.getArgs(), "GOTO", 1);
                    int number = intFromBytes(opCode.getArgs()[0]);
                    Integer line = labels.get(number);
                    if (line == null) {
                        while (true) {
                            i++;
                            if (i >= opCodes.size())
                                throw new RuntimeException("Error in opcode GOTO: Label " + number + " does not exist");
                            if (opCodes.get(i).getType() == OpCode.Type.LABEL) {
                                checkParams(opCodes.get(i).getArgs(), "LABEL", 1);
                                if (number == intFromBytes(opCodes.get(i).getArgs()[0])) {
                                    break;
                                } else {
                                    labels.put(intFromBytes(opCodes.get(i).getArgs()[0]), i);
                                }
                            }
                        }
                    } else i = line;
                } break;
                case PRINT_QUACK: {
                    if (opCode.getArgs().length > 0)
                        throw new RuntimeException("Quacking does not requackire parameters!");
                    System.out.println("quack");
                } break;
                case RETURN: {
                    ret = stackAccessor.get();
                } break kar;
            }
        }
        return isFunction ? ret : env;
    }

    public class BTRESignature {
        private List<BTREClass> argumentClasses;
        private BTREClass returnType;

        public BTRESignature(List<BTREClass> argumentClasses, BTREClass returnType) {
            this.argumentClasses = argumentClasses;
            this.returnType = returnType;
        }

        public List<BTREClass> getArgumentClasses() {
            return argumentClasses;
        }

        public BTREClass getReturnType() {
            return returnType;
        }
    }

    public class BTREClass extends BTREClassMember {
        private String name;
        private Map<String, BTREClassMember> classMembers = new HashMap<>();
        private Map<BTREClass, BTREMethod> casts = new HashMap<>();
        private BTREClass parent;

        public Map<BTREClass, BTREMethod> getCasts() {
            return casts;
        }

        public String getName() {
            return name;
        }

        public Map<String, BTREClassMember> getClassMembers() {
            return parent == null ? classMembers : joinMaps(classMembers, parent.getClassMembers());
        }

        public BTREClass extend(String name) {
            return new BTREClass(name, this);
        }

        public BTREClass getParent() {
            return parent;
        }

        public BTREClass(String name, Map<String, BTREClassMember> classMembers, BTREClass parent) {
            this.name = name;
            this.classMembers = classMembers;
            this.parent = parent;
        }

        public BTREClass(String name, BTREClass parent) {
            this.name = name;
            this.parent = parent;
        }

        public List<BTREClass> getAllAssignable() {
            List<BTREClass> result = new ArrayList<>();
            result.add(this);
            if (parent != null) result.addAll(parent.getAllAssignable());
            for (Map.Entry<BTREClass, BTREMethod> entry : casts.entrySet())
                result.addAll(entry.getKey().getAllAssignable());
            return result;
        }

        public boolean isAssignableFrom(BTREClass btreClass) {
            return btreClass.getAllAssignable().contains(this);
        }
    }

    private static <T, E> Map<T, E> joinMaps(Map<T, E> map1, Map<T, E> map2) {
        Map<T, E> result = new HashMap<>();
        for (Map.Entry<T, E> entry : map1.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<T, E> entry : map2.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public class BTREClassMember {
        protected BTREModifiers modifiers;

        public BTREModifiers getModifiers() {
            return modifiers;
        }
    }

    public class BTREField extends BTREClassMember {
        private BTREClass type;

        public BTREClass getType() {
            return type;
        }

        public BTREField(BTREModifiers modifiers, BTREClass type) {
            this.modifiers = modifiers;
            this.type = type;
        }
    }

    public class BTREProperty extends BTREClassMember {
        private BTREClass type;
        private BTREMethod getMethod;
        private BTREMethod setMethod;

        public BTREProperty(BTREModifiers modifiers, BTREClass type, BTREMethod getMethod, BTREMethod setMethod) {
            this.modifiers = modifiers;
            this.type = type;
            this.getMethod = getMethod;
            this.setMethod = setMethod;
        }

        public BTREClass getType() {
            return type;
        }

        public BTREMethod getGetMethod() {
            return getMethod;
        }

        public BTREMethod getSetMethod() {
            return setMethod;
        }
    }

    public class BTREMethod extends BTREClassMember {
        private byte[] code;
        private BTRESignature signature;

        public byte[] getCode() {
            return code;
        }

        public BTRESignature getSignature() {
            return signature;
        }

        public BTREObject invoke(Environment environment, BTREObject self, BTREObject... args) {
            return executeFunction(code, environment, self, args);
        }
    }

    public class BTREModifiers {
        private boolean _static;
        private boolean _const;
        private BTREAccessModifiers accessModifier;

        public BTREModifiers(boolean _static, boolean _const, BTREAccessModifiers accessModifier) {
            this._static = _static;
            this._const = _const;
            this.accessModifier = accessModifier;
        }

        public boolean isStatic() {
            return _static;
        }

        public void setStatic(boolean _static) {
            this._static = _static;
        }

        public boolean isConst() {
            return _const;
        }

        public void setConst(boolean _const) {
            this._const = _const;
        }

        public BTREAccessModifiers getAccessModifier() {
            return accessModifier;
        }

        public void setAccessModifier(BTREAccessModifiers accessModifier) {
            this.accessModifier = accessModifier;
        }
    }

    public enum BTREAccessModifiers {
        PUBLIC((byte) 0x01), FILEPRIVATE((byte) 0x02), PROTECTED((byte) 0x03), PRIVATE((byte) 0x04);
        private byte code;

        BTREAccessModifiers(byte code) {
            this.code = code;
        }

        public byte getCode() {
            return code;
        }
    }
}

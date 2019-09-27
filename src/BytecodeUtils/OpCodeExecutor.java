package BytecodeUtils;

import OpCodes.OpCode;

import java.lang.reflect.Array;
import java.util.*;

public class OpCodeExecutor {


    public static class Environment {
        private List<BTREObject> variables = new ArrayList<>();
        private Map<String, BTREClass> classes = new HashMap<>();

        public List<BTREObject> getVariables() {
            return variables;
        }

        public void setVariables(List<BTREObject> variables) {
            this.variables = variables;
        }

        public Map<String, BTREClass> getClasses() {
            return classes;
        }

        public void setClasses(Map<String, BTREClass> classes) {
            this.classes = classes;
        }

        public Environment(List<BTREObject> variables, Map<String, BTREClass> classes) {
            this.variables = variables;
            this.classes = classes;
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

    private static Object runBytecode(byte[] code, Environment environment, BTREObject self, BTREObject... variables) {
        boolean isFunction = self != null && variables != null;
        Environment env = isFunction ? new Environment(
                new ArrayList<BTREObject>(){{
                    add(self); addAll(Arrays.asList(variables));
                }},
                environment.classes
        ) : environment;
        BTREObject ret = null;
        List<OpCode> opCodes = BytecodeParser.parseBytecode(code);
        Stack<BTREObject> stack = new Stack<>();
        for (int i = 0; i < opCodes.size(); i++) {
            // TODO!!
            switch (opCodes.get(i).getType()) {
                case INCLUDE:

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
        private BTREClass parent;

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
    }

    private static<T, E> Map<T, E> joinMaps(Map<T, E> map1, Map<T, E> map2) {
        Map<T, E> result = new HashMap<>();
        for (Map.Entry<T, E> entry: map1.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<T, E> entry: map2.entrySet()) {
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

        public byte[] getCode() {
            return code;
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

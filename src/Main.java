import FileUtils.FileManager;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    private static class UniqueOrWarnMap<T, E> extends HashMap<T, E> {
        @Override
        public E put(T key, E value) {
            if (this.get(key) != null) System.err.println("Warning: key \"" + key + "\" already exists, overriding");
            return super.put(key, value);
        }
    }

    private static Map<String, Object> parseArgs(String[] args, String parameterPrefix) {
        String prevArg = null;
        Map<String, Object> result = new UniqueOrWarnMap<>();

        for (String arg: args) {
            if (prevArg == null) {
                if (arg.startsWith(parameterPrefix)) {
                    prevArg = arg.substring(parameterPrefix.length());
                } else {
                    throw new RuntimeException("Parameter name expected");
                }
            } else {
                if (arg.startsWith(parameterPrefix)) {
                    result.put(prevArg, new None());
                    prevArg = arg.substring(parameterPrefix.length());
                } else {
                    Object entry = null;
                    try {
                        entry = Long.parseLong(arg);
                    } catch (NumberFormatException e) {
                        try {
                            entry = Double.parseDouble(arg);
                        } catch (NumberFormatException e1) {
                            entry = arg;
                        }
                    }
                    result.put(prevArg, entry);
                    prevArg = null;
                }
            }
        }

        return result;
    }

    private static class None {}

    public static void main(String[] args) {
        Map<String, Object> params = parseArgs(args, "-");
        ArrayList<File> filePath = new ArrayList<>();
        if (System.getenv("BTREPATH") != null) {
            filePath.addAll(FileManager.parseFilePath(System.getenv("BTREPATH")));
        }
        for (Map.Entry<String, Object> entry: params.entrySet()) {
            if (entry.getKey().equals("filepath")) {
                if (entry.getValue().getClass() == None.class) {
                    throw new RuntimeException("filepath parameter needs a value");
                }
                filePath.addAll(FileManager.parseFilePath(entry.getValue().toString()));
            } else {
                throw new RuntimeException("Unknown parameter \"" + entry.getKey() + "\"");
            }
        }
    }
}

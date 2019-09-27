package FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FileManager {
    public static List<File> parseFilePath(String filePath) {
        ArrayList<File> result = new ArrayList<>();

        for (String path: filePath.split(";")) {
            String[] components = path.split(Pattern.quote(File.separator));
            String lastComponent = components[components.length - 1];
            if (lastComponent.equals("*")) {
                result.addAll(
                        getAllInPath(
                                new File(
                                        String.join(
                                                File.separator,
                                                Arrays.copyOf(components, components.length - 1)
                                        )
                                )
                        )
                );
            } else if (lastComponent.equals("**")) {
                result.addAll(
                        getAllInPathRecursive(
                                new File(
                                        String.join(
                                                File.separator,
                                                Arrays.copyOf(components, components.length - 1)
                                        )
                                )
                        )
                );
            } else {
                File res = new File(
                        String.join(
                                File.separator,
                                components
                        )
                );
                if (res.exists()) result.add(res); else throw new RuntimeException("Path \"" + path + "\" does not exist");
            }
        }

        return result;
    }

    public static List<File> getAllInPath(File directory) {
        assert directory.exists() && directory.isDirectory();
        return Arrays.asList(directory.listFiles());
    }

    public static List<File> getAllInPathRecursive(File directory) {
        assert directory.exists() && directory.isDirectory();
        ArrayList<File> result = new ArrayList<>();
        for (File file: getAllInPath(directory)) {
            if (file.isDirectory()) {
                result.addAll(getAllInPathRecursive(file));
            } else {
                result.add(file);
            }
        }
        return result;
    }

    public static File getFile(List<File> filePath, String path) {
        File toFind = new File(path);
        for (File file: filePath) {
            try {
                if (Files.isSameFile(Paths.get(file.toURI()), Paths.get(toFind.toURI()))) {
                    return file;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static byte[] readFile(File file) throws IOException {
        return Files.readAllBytes(Paths.get(file.toURI()));
    }
}

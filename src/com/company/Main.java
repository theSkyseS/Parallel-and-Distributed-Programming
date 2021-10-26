package com.company;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        List<Path> javaFiles = Files.walk(Path.of("./src")).filter(Files::isRegularFile).collect(Collectors.toList());
        javaFiles.stream().map(Main::Mapper).forEach(classMap -> {
            classMap.forEach((x, y) -> {
                System.out.println(x + ": ");
                y.forEach(z -> System.out.print("   " + z + " "));
                System.out.println();
            });
        });
            /*;
            implementsMap.forEach((x, y) -> {
                System.out.println(x + ": ");
                y.forEach(z -> System.out.print("   " + z + " "));
                System.out.println();
            });*/

    }

    private static Map<String, List<String>> Mapper(Path javaFile) {
        Map<String, List<String>> classMap = new HashMap<>();
        Map<String, List<String>> implementsMap = new HashMap<>();
        Scanner sc = null;
        try {
            sc = new Scanner(javaFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sc.useDelimiter("\\Z");

        String code = sc.next();
        Pattern classRegex = Pattern.compile("^((static\\s+)?(abstract\\s+)?((public|private|protected)\\s+)?(static\\s+)?(abstract\\s+)?)?(class|interface)\\s+(?<class>\\w+)\\s*(extends\\s+(?<extends>\\w*))?\\s*(implements\\s+(?<implements>(\\w+(,\\s+)?)*))?", Pattern.MULTILINE);
        Matcher matcher = classRegex.matcher(code);
        while (matcher.find()) {
            String classString = matcher.group("class");
            String extendsString = matcher.group("extends");
            String implementsString = matcher.group("implements");
            if (extendsString != null) {
                if (!classMap.containsKey(extendsString)) {
                    List<String> putList = new ArrayList<>();
                    putList.add(classString);
                    classMap.put(extendsString, putList);
                } else {
                    classMap.get(extendsString).add(classString);
                }
            }
            if (implementsString != null) {
                if (!implementsMap.containsKey(implementsString)) {
                    List<String> putList = new ArrayList<>();
                    putList.add(classString);
                    implementsMap.put(implementsString, putList);
                } else {
                    implementsMap.get(implementsString).add(classString);
                }
            }
        }
        return classMap;
    }
}

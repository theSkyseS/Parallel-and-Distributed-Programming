package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Path> javaFiles = Files.walk(Path.of("../spring-framework-main")).filter(Files::isRegularFile).filter(x -> x.getFileName().toString().endsWith(".java")).collect(Collectors.toList());
        CountDownLatch latch = new CountDownLatch(javaFiles.size());
        Map<String, List<String>> classMap = new HashMap<>();
        for(var javaFile: javaFiles) {
            Mapper mapper = new Mapper(javaFile, latch);
            mapper.run();
            putMergeAll(classMap, mapper.getClassMap());
        }
        latch.await();
        classMap.forEach((x, y) -> {
            System.out.println(x + ": ");
            y.forEach(z -> System.out.println("   " + z));
            System.out.println();
        });
        System.out.println(classMap.size());
    }

    private static void putMergeAll(final Map<String, List<String>> map, final Map<String, List<String>> mapToPut) {
        mapToPut.forEach((key, value) -> {
            if (!map.containsKey(key)) {
                map.put(key, value);
            } else {
                map.get(key).addAll(value);
            }
        });
    }

}

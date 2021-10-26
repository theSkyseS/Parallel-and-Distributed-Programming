package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class Main extends Object {

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Path> javaFiles = Files.walk(Path.of("./spring-framework-main")).filter(Files::isRegularFile).filter(x -> x.getFileName().toString().endsWith(".java")).collect(Collectors.toList());
        CountDownLatch latch = new CountDownLatch(javaFiles.size());
        Map<String, List<String>> classMap = new HashMap<>();
        for(var javaFile: javaFiles) {
            Mapper mapper = new Mapper(javaFile, latch);
            mapper.run();
            classMap.putAll(mapper.getClassMap());
        }
        latch.await();
        System.out.println(classMap.size());
        classMap.forEach((x, y) -> {
            System.out.println(x + ": ");
            y.forEach(z -> System.out.println("   " + z));
            System.out.println();
        });

    }
}

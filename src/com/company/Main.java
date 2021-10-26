package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Path> javaFiles = Files.walk(Path.of("../spring-framework-main")).filter(Files::isRegularFile).filter(x -> x.getFileName().toString().endsWith(".java")).collect(Collectors.toList());
        CountDownLatch latch = new CountDownLatch(javaFiles.size());
        ReentrantLock lock = new ReentrantLock();
        Map<String, List<String>> classMap = new HashMap<>();
        for(var javaFile: javaFiles) {
            Mapper mapper = new Mapper(javaFile, latch, lock, classMap);
            mapper.run();
        }
        latch.await();
        classMap.forEach((x, y) -> {
            System.out.println(x + ": ");
            y.forEach(z -> System.out.println("   " + z));
            System.out.println();
        });
        System.out.println(classMap.size());
    }
}

package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {

    private static final int THREAD_COUNT = 16;

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Path> javaFiles = Files.walk(Path.of("../spring-framework-main")).filter(Files::isRegularFile).filter(x -> x.getFileName().toString().endsWith(".java")).collect(Collectors.toList());
        Path poisonPill = Path.of("./src/com/company/Main.java");
        BlockingQueue<Path> fileQueue = new ArrayBlockingQueue<>(javaFiles.size() + THREAD_COUNT);
        BlockingQueue<Map<String, List<String>>> classMapQueue = new LinkedBlockingQueue<>();

        Map<String, List<String>> classMap = new HashMap<>();
        for (var javaFile : javaFiles) {
            fileQueue.put(javaFile);
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            fileQueue.put(poisonPill);
        }

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(new Mapper(latch, fileQueue, classMapQueue)).start();
        }

        latch.await();
        System.out.println("Latch awaited count:" + latch.getCount());
        classMapQueue.forEach(x -> putMergeAll(classMap, x));
        classMapQueue.forEach(x -> System.out.println(x.size()));
        printMap(classMap);
    }

    public static void printMap(final Map<String, List<String>> map) {
        AtomicInteger counter = new AtomicInteger();
        map.forEach((x, y) -> {
            System.out.println(x + ": ");
            y.forEach(z -> {
                counter.getAndIncrement();
                System.out.println("   " + z);
            });
            System.out.println();
        });
        System.out.println(map.size());
        System.out.println(counter);
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

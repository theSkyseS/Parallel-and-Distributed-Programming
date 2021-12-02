package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {

    public static final int THREAD_COUNT = 16;

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Optional<Path>> javaFiles = Files.walk(Path.of("../spring-framework-main"))
                .filter(Files::isRegularFile)
                .filter(x -> x.getFileName().toString().endsWith(".java"))
                .map(Optional::of)
                .collect(Collectors.toList());
        BlockingQueue<Optional<Path>> fileQueue = new ArrayBlockingQueue<>(20);
        BlockingQueue<Map<String, List<String>>> classMapQueue = new LinkedBlockingQueue<>();
        List<Map<String, List<String>>> classMapList = new ArrayList<>();

        Map<String, List<String>> classMap = new HashMap<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(new Mapper(fileQueue, classMapQueue)).start();
        }

        new Thread(() -> {
            try {
                for (var javaFile : javaFiles) {
                    fileQueue.put(javaFile);
                }
                for (int i = 0; i < Main.THREAD_COUNT; i++) {
                    fileQueue.put(Optional.empty());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        for(int i=0; i < THREAD_COUNT; i++) {
            classMapList.add(classMapQueue.take());
        }

        classMapList.forEach(x -> putMergeAll(classMap, x));
        classMapList.forEach(x -> System.out.println(x.size()));

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

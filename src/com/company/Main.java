package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Path> javaFiles = Files.walk(Path.of("../spring-framework-main")).filter(Files::isRegularFile).filter(x -> x.getFileName().toString().endsWith(".java")).collect(Collectors.toList());
        ExecutorService service = Executors.newFixedThreadPool(4);

        //CountDownLatch latch = new CountDownLatch(javaFiles.size());
        //ReentrantLock lock = new ReentrantLock();
        List<Future<Map<String, List<String>>>> futuresList = new ArrayList<>();
        Map<String, List<String>> classMap = new HashMap<>();
        for (var javaFile : javaFiles) {
            Future<Map<String, List<String>>> future = service.submit(() -> Mapper.run(javaFile));
            futuresList.add(future);
            //Mapper mapper = new Mapper(javaFile, latch, lock, classMap);
            //mapper.run();
        }

        service.shutdown();

        for (var future : futuresList) {
            try {
                for (var entry : future.get().entrySet()) {
                    String key = entry.getKey();
                    if (classMap.containsKey(key)) {
                        classMap.get(key).addAll(entry.getValue());
                    } else {
                        classMap.put(entry.getKey(), entry.getValue());
                    }
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        //latch.await();
        classMap.forEach((x, y) -> {
            System.out.println(x + ": ");
            y.forEach(z -> System.out.println("   " + z));
            System.out.println();
        });
        System.out.println(classMap.size());
        System.out.println((Integer) classMap.values().stream().map(List::size).mapToInt(x -> x).sum());
    }
}

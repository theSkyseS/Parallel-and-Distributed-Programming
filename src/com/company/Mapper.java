package com.company;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Mapper implements Runnable {
    private static int count = 0;

    private final Path poisonPill = Path.of("./src/com/company/Main.java");
    private final Map<String, List<String>> classMap;
    private final BlockingQueue<Path> inQueue;
    private final BlockingQueue<Map<String, List<String>>> outQueue;
    private final CountDownLatch latch;
    private final int ThreadNumber;


    public Mapper(final CountDownLatch latch, final BlockingQueue<Path> inQueue, final BlockingQueue<Map<String, List<String>>> outQueue) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.latch = latch;
        this.classMap = new HashMap<>();
        count++;
        ThreadNumber = count;
    }

    public void run() {
        Path taken;
        while (true) {
            Scanner sc;
            try {
                taken = inQueue.take();
                System.out.println("Thread " + ThreadNumber + " taken file: " + taken);
                if (taken.equals(this.poisonPill)) {
                    outQueue.add(classMap);
                    System.out.println("Poison pill taken by " + ThreadNumber);
                    latch.countDown();
                    System.out.println("Latch Count Down" + latch.getCount());
                    return;
                }
                sc = new Scanner(taken);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                break;
            }
            sc.useDelimiter("\\Z");
            String code;
            try {
                code = sc.next();
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                break;
            }

            Pattern classRegex = Pattern.compile("^(?:(static\\s+)?(abstract\\s+)?((public|private|protected)\\s+)?(static\\s+)?(abstract\\s+)?)?(?:class|interface)\\s+(?<class>\\w+(<.*?(?<=>(\\s|.$))+)?)\\s*(extends\\s+(?<extends>\\w*(<.*?>+)?))?\\s*(implements\\s+(?<implements>(\\w+(<.*>+)?(,\\s+)?)*))?", Pattern.MULTILINE);
            Matcher matcher = classRegex.matcher(code);
            try {
                while (matcher.find()) {
                    String classString = matcher.group("class");
                    String extendsString = matcher.group("extends");
                    String implementsString = matcher.group("implements");

                    if (classString == null) {
                        continue;
                    }

                    if (extendsString != null) {
                        extendsString = extendsString.trim();
                        putMerge(classMap, classString, extendsString);
                    }

                    if (implementsString != null) {
                        List<String> implementes = Arrays.stream(implementsString.replaceAll("\\s+", "").split(" ")).flatMap(x -> Arrays.stream(StringExt.split(x, "(?:<.*?>)|(?<semicolon>,)", "semicolon"))).collect(Collectors.toList());
                        for (String implement : implementes) {
                            if (implement.length() == 0) continue;
                            putMerge(classMap, classString, implement);
                        }
                    }
                }
            } catch (Exception e) {
                break;
            }
        }
        latch.countDown();
        System.out.println("Latch Count Down" + latch.getCount());
        outQueue.add(classMap);
        Thread.currentThread().interrupt();
    }

    private static void putMerge(final Map<String, List<String>> classMap, final String classString, final String implement) {
        if (!classMap.containsKey(implement)) {
            List<String> putList = new ArrayList<>();
            putList.add(classString);
            classMap.put(implement, putList);
        } else {
            classMap.get(implement).add(classString);
        }
    }

}

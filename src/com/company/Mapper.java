package com.company;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Mapper implements Runnable {

    private Map<String, List<String>> classMap;
    private final Scanner sc;
    private CountDownLatch latch;

    public Mapper(Path javaFile, CountDownLatch latch) throws IOException {
        Thread thread = new Thread(this);
        this.classMap = new HashMap<>();
        this.latch = latch;
        this.sc = new Scanner(javaFile);
        thread.start();
    }

    public Map<String, List<String>> getClassMap() {
        return classMap;
    }

    @Override
    public void run() {
        sc.useDelimiter("\\Z");
        String code;
        synchronized (sc) {
            try {
                code = sc.next();
            } catch (NoSuchElementException e) {
                latch.countDown();
                return;
            }
        }
        Pattern classRegex = Pattern.compile("^(?:(static\\s+)?(abstract\\s+)?((public|private|protected)\\s+)?(static\\s+)?(abstract\\s+)?)?(?:class|interface)\\s+(?<class>\\w+(<.*?>+)?)\\s*(extends\\s+(?<extends>\\w*(<.*?>+)?))?\\s*(implements\\s+(?<implements>(\\w+(<.*?>+)?(,\\s+)?)*))?", Pattern.MULTILINE);
        Matcher matcher = classRegex.matcher(code);
        while (matcher.find()) {
            String classString = matcher.group("class");
            String extendsString = matcher.group("extends");
            String implementsString = matcher.group("implements");
            if (extendsString != null) {
                extendsString = extendsString.trim();
                if (!classMap.containsKey(extendsString)) {
                    List<String> putList = new ArrayList<>();
                    putList.add(classString);
                    classMap.put(extendsString, putList);
                } else {
                    classMap.get(extendsString).add(classString);
                }
            }
            if (implementsString != null) {
                List<String> implementes = Arrays.stream(implementsString.replaceAll("\\s+", "").split(" ")).flatMap(x -> Arrays.stream(StringExt.split(x, "(?:<.*?>)|(?<semicolon>,)", "semicolon"))).collect(Collectors.toList());
                for (String implement : implementes) {
                    if (implement.length() == 0) continue;
                    if (!classMap.containsKey(implement)) {
                        List<String> putList = new ArrayList<>();
                        putList.add(classString);
                        classMap.put(implement, putList);
                    } else {
                        classMap.get(implement).add(classString);
                    }
                }
            }
        }
        latch.countDown();
    }
}

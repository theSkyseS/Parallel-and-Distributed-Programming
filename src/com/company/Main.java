package com.company;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Main {

    public static void main(String[] args) throws IOException {
        String fileName = "text.txt";
        Files.lines(Path.of(fileName), StandardCharsets.UTF_8)
                .flatMap(line -> Stream.of(line.split("\\W+")))
                .filter(x -> x.length() > 0)
                .map(String::toLowerCase)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((x, y) -> System.out.println(x + "   " + y));
    }
}

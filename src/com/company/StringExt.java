package com.company;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringExt {
    public static String[] split(String input, String regex, String group) {
        int index = 0;
        ArrayList<String> matchList = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(input);

        // Add segments before each match found
        while (m.find()) {
            {
                int end = m.start(group);
                if (end == -1) continue;
                String match = input.subSequence(index, m.start(group)).toString();
                matchList.add(match);
                index = m.end();
            }
        }
        return matchList.toArray(String[]::new);
    }
}

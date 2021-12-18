package Utils;


import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Mapper {
    public static Map<String, List<String>> map(String javaFile) {
        Map<String, List<String>> classMap = new HashMap<>();

        Pattern classRegex = Pattern.compile("^(?:(static\\s+)?(abstract\\s+)?((public|private|protected)\\s+)?(static\\s+)?(abstract\\s+)?)?(?:class|interface)\\s+(?<class>\\w+(<.*?(?<=>(\\s|.$))+)?)\\s*(extends\\s+(?<extends>\\w*(<.*?>+)?))?\\s*(implements\\s+(?<implements>(\\w+(<.*>+)?(,\\s+)?)*))?", Pattern.MULTILINE);
        Matcher matcher = classRegex.matcher(javaFile);
        while (matcher.find()) {
            String classString = matcher.group("class");
            String extendsString = matcher.group("extends");
            String implementsString = matcher.group("implements");

            if (classString == null) {
                continue;
            }

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
        return classMap;
    }
}


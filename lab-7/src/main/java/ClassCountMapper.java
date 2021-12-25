import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClassCountMapper
        extends Mapper<Object, Text, Text, Text> {
    public void map(final Object key, final Text value, final Context context)
            throws IOException, InterruptedException {
        Pattern classRegex = Pattern
                .compile("^(?:(static\\s+)?(abstract\\s+)?((public|private|protected)\\s+)?"
                                + "(static\\s+)?(abstract\\s+)?)?"
                                + "(?:class|interface)\\s+(?<class>\\w+(<.*?(?<=>(\\s|.$))+)?)\\s*"
                                + "(extends\\s+(?<extends>\\w*(<.*?>+)?))?\\s*"
                                + "(implements\\s+(?<implements>(\\w+(<.*>+)?(,\\s+)?)*))?",
                        Pattern.MULTILINE);
        Matcher matcher = classRegex.matcher(String.valueOf(value));
        while (matcher.find()) {
            String classString = matcher.group("class");
            String extendsString = matcher.group("extends");
            String implementsString = matcher.group("implements");

            if (classString == null) {
                continue;
            }

            Text classText = new Text(classString);
            if (extendsString != null) {
                extendsString = extendsString.trim();
                context.write(new Text(extendsString), classText);
            }

            if (implementsString != null) {
                Arrays.stream(implementsString
                                .replaceAll("\\s+", "")
                                .split(" ")
                        ).flatMap(x -> Arrays.stream(
                                StringExt.split(x, "(?:<.*?>)|(?<semicolon>,)", "semicolon")))
                        .filter(x -> x.length() > 0)
                        .forEach(implement -> {
                            try {
                                context.write(new Text(implement), classText);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
    }
}

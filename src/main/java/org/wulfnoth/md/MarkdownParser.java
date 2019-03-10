package org.wulfnoth.md;

import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.options.MutableDataSet;
import org.apache.commons.lang3.StringUtils;
import org.wulfnoth.gadus.md.handle.TableHandler;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class MarkdownParser {

    private List<CustomLabel> labels;
    private String content;
    private Map<String, String> placeholders;

    public MarkdownParser(String content) {
        CustomLabel label = new CustomLabel("$$", false, CustomLabel.KEEP);
        labels = new ArrayList<>();
        labels.add(label);
        placeholders = new HashMap<>();
        this.content = content;
    }

    private Set<String> findLabelCrossLine(String label) {
        Set<String> result = new HashSet<>();
        int startIndex = 0 - label.length();
        int findIndex;
        boolean findLeft = true;
        while ((findIndex = StringUtils.indexOf(content, label, startIndex+label.length())) != -1) {
            if (findLeft) {
                startIndex = findIndex;
                findLeft = false;
            } else {
                findLeft = true;
//                String placeholder = "\nreplace" + random.nextLong() + "replace\n";
//                placeholders.put(placeholder.substring(1, placeholder.length()-1), content.substring(startIndex, findIndex+2));
                result.add(content.substring(startIndex, findIndex + label.length()));
                startIndex = findIndex;
            }
        }

        return result;
    }

    private Map<String, String> findLabelInLine() {
        return null;
    }

    private String customReplace(List<CustomLabel> list) {
        final Random random = new Random(System.currentTimeMillis());
        Set<Long> usedHolder = new HashSet<>();
        list.forEach(customLabel -> {
            if (customLabel.isInline()) {
                findLabelInLine();
                // todo
            } else {
                findLabelCrossLine(customLabel.getLabel()).forEach(c -> {
                    long holder;
                    do {
                        holder = random.nextLong();
                    } while (usedHolder.contains(holder));
                    usedHolder.add(holder);
                    String placeholder = "replace" + holder + "replace";
                    placeholders.put(placeholder, c);
                    content = StringUtils.replace(content, c, String.format("\n%s\n", placeholder));
                });
            }
        });


//        for (String key : placeholders.keySet()) {
//            content = StringUtils.replace(content, placeholders.get(key), key);
//        }
        return content;
    }

    private String restoreMathJax() {
        for (String key : placeholders.keySet()) {
            content = StringUtils.replace(content, key, placeholders.get(key));
        }
        return content;
    }

    private FinalPage handle() {
        placeholders.clear();
        final FinalPage page = new FinalPage();

        content = customReplace(labels);

        content = TableHandler.handle(content);

        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);
//        options.set(Parser.EXTENSIONS, Collections.singletonList(TablesExtension.create()));
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(content);

        content = renderer.render(document);
        page.setContent(restoreMathJax());
        return page;
    }

    public static FinalPage parser(String content) {

        return new MarkdownParser(content).handle();
    }

    public static FinalPage parser(File file) {
        List<String> list = new ArrayList<>();

        try(InputStream stream = new FileInputStream(file)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
            list = reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        final StringBuilder sb = new StringBuilder();
        list.forEach(line -> sb.append(line).append("\n"));
//        System.out.println(sb.toString());
        FinalPage finalPage = parser((sb.toString()));
        finalPage.replaceImgTag(file.getParent());
        return finalPage;
    }
}

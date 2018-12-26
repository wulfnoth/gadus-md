package org.wulfnoth.md;

import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.options.MutableDataSet;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class MarkdownParser {

    private static Map<String, String> placeholders = new HashMap<>();

    private static String replaceMathJax(String content) {

        Random random = new Random();
        int startIndex = -2;
        int findIndex;
        boolean findLeft = true;
        while ((findIndex = StringUtils.indexOf(content, "$$", startIndex+2)) != -1) {
            if (findLeft) {
                startIndex = findIndex;
                findLeft = false;
            } else {
//                System.out.println(content.substring(startIndex, findIndex+2));
                findLeft = true;
                String placeholder = "\nreplace" + random.nextLong() + "replace\n";
                placeholders.put(placeholder.substring(1, placeholder.length()-1), content.substring(startIndex, findIndex+2));
//                content = StringUtils.replace(content, content.substring(startIndex, findIndex+2), placeholder);
                startIndex = findIndex;
            }
        }
        for (String key : placeholders.keySet()) {
            content = StringUtils.replace(content, placeholders.get(key), key);
        }
//        System.out.println(placeholders.size());

        return content;
    }

    private static String restoreMathJax(String content) {
        if (content.contains("<h1>Tree Boosting</h1>"))
            System.out.println(placeholders.size());
        for (String key : placeholders.keySet()) {
//            System.out.println(placeholders.get(key));
            content = StringUtils.replace(content, key, placeholders.get(key));
        }
//        System.out.println(content);
//        for (String key : placeholders.keySet()) {
////            System.out.println(placeholders.get(key));
//            System.out.println(key);
//        }
        return content;
    }

    public static FinalPage parser(File file) {
        placeholders.clear();
        final FinalPage page = new FinalPage();
        List<String> list = new ArrayList<>();

        try(InputStream stream = new FileInputStream(file))
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
            list = reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        final StringBuilder sb = new StringBuilder();
        list.forEach(line -> {
            String h = InfoExtractor.extractHeader1(line);
            if (h != null)
                page.setHeader(h);
            sb.append(line).append("\n");
        });
        String content = replaceMathJax(sb.toString());

//        System.out.println(content);

        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);
        options.set(Parser.EXTENSIONS, Collections.singletonList(TablesExtension.create()));
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(content);

        page.setContent(restoreMathJax(renderer.render(document)));

//        if (page.getContent().contains("<h1>Tree Boosting</h1>"))
//            System.out.println(page.getContent());

        return page;
    }

}

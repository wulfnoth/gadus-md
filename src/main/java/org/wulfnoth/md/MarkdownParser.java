package org.wulfnoth.md;

import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.options.MutableDataSet;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MarkdownParser {

    public static FinalPage parser(File file) {

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
        String content = sb.toString();

        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);
        options.set(Parser.EXTENSIONS, Collections.singletonList(TablesExtension.create()));
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(content);

        page.setContent(renderer.render(document));

        return page;
    }

}

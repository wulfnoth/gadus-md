package org.wulfnoth.md;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FinalPage {

    private String header = null;
    private String content = null;

    public void setHeader(String header) {
        if (this.header == null)
            this.header = header;
    }

    private static String getImgStr(String imgFile){

        //读取图片字节数组
        try (InputStream in = new FileInputStream(imgFile))
        {
            byte[] data = new byte[in.available()];
            in.read(data);
            return new String(Base64.encodeBase64(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String replacePreTag(String content) {
        return content.replaceAll("<pre>", "<pre class=\"sh_python\">");
    }

    public void replaceImgTag(String parentDir) {
        String srcImage = "<p><img src=\".*\" alt=\".*\" /></p>";
        StringBuilder sb = new StringBuilder();
        for (String line : content.split("\n")) {
            if (line.matches(srcImage)) {
                Pattern p = Pattern.compile("src=\"(.*?)\"");
                Matcher m = p.matcher(line);
                if (m.find() && m.groupCount() == 1) {
                    String position = m.group(1);
                    String base64Img = getImgStr(parentDir + File.separator + position);
                    String format = position.substring(position.lastIndexOf(".") + 1);
                    sb.append("<img src=\"data:image/").append(format).append(";base64,").append(base64Img).append("\"/>");
//                    System.out.println(base64Img);
                } else {
                    sb.append(line);
                }
            } else {
                sb.append(line);
            }
            sb.append("\n");
        }
        content = sb.toString();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = replacePreTag(content);
    }

    public String getHTML(Map<String, String> resourceInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n<meta content=\"charset=utf-8\"/>\n<title>")
                .append(header).append("</title>\n")
                .append("<link type=\"text/css\" rel=\"stylesheet\" href=\"css/sh_nedit.css\"/>\n")
                .append(String.format("<link rel=\"stylesheet\" href=\"%s/content.css\" type=\"text/css\"/>\n", resourceInfo.get("css")))
                .append(String.format("<script type=\"text/javascript\" src=\"%s/sh_main.js\"></script>\n", resourceInfo.get("js")))
                .append(String.format("<script type=\"text/javascript\" src=\"%s/sh_python.js\"></script>\n", resourceInfo.get("js")))
                .append("<script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"></script>\n")
                .append("<script type=\"text/x-mathjax-config\">\n" +
                        "    MathJax.Hub.Config({\n" +
                        "        tex2jax: {inlineMath: [['$', '$']]},\n" +
                        "        messageStyle: \"none\"\n" +
                        "    });\n" +
                        "</script>\n")
                .append("</head>\n<body onload=\"sh_highlightDocument()\">\n")
                .append("<div>\n")
                .append(content).append("\n")
                .append("</div>\n")
                .append("</body>\n")
                .append("</html>");

        return sb.toString();
    }

}

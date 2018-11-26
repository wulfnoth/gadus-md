package org.wulfnoth.md;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.net.FileRetrieve;
import com.itextpdf.tool.xml.net.ReadingProcessor;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;

public class Dumper {

    public static String CHARSET_NAME = "UTF-8";

//    public static void generate(String htmlStr, OutputStream out)
//            throws Exception {
//        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
//                .newDocumentBuilder();
//        org.w3c.dom.Document doc = builder.parse(new ByteArrayInputStream(htmlStr
//                .getBytes()));
//        ITextRenderer renderer = new ITextRenderer();
//        renderer.setDocument(doc, null);
//        renderer.layout();
//        renderer.createPDF(out);
//        out.close();
//    }

    public static String getFileContent(String filepath) {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream in = new FileInputStream(filepath)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static void main(String[] args) {

        String htmlStr = getFileContent("D:/test/Text_Data_Flattening_Filtering_Chunking.html");
        String cssStr = getFileContent("D:/test/");

//        try (FileOutputStream out = new FileOutputStream("D:/test/result.pdf")){
//            System.out.println(sb.toString());
//            generatePlus(sb.toString(), out);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }
    }

    public static void htmlToPdf(String htmlstr,String cssSource) throws Exception
    {
        String outputFile = "D:/test.pdf";
        Document document = new Document();
        PdfWriter writer;
        writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        InputStream bis = new ByteArrayInputStream(htmlstr.getBytes());
        InputStream cssis = new ByteArrayInputStream(cssSource.getBytes());
        XMLWorkerHelper.getInstance().parseXHtml(writer, document, bis,cssis);

        document.close();
    }


    public static void generatePlus(String htmlStr, OutputStream out) throws IOException, DocumentException {
        final String charsetName = "UTF-8";

        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        // html内容解析
        HtmlPipelineContext htmlContext = new HtmlPipelineContext(
                new CssAppliersImpl(new XMLWorkerFontProvider() {
                    @Override
                    public Font getFont(String fontname, String encoding,
                                        float size, final int style) {
                        if (fontname == null) {
                            // 操作系统需要有该字体, 没有则需要安装; 当然也可以将字体放到项目中， 再从项目中读取
                            fontname = "SimSun";
                        }
                        return super.getFont(fontname, encoding, size,
                                style);
                    }
                })) {
            @Override
            public HtmlPipelineContext clone()
                    throws CloneNotSupportedException {
                HtmlPipelineContext context = super.clone();
                try {
                    ImageProvider imageProvider = this.getImageProvider();
                    context.setImageProvider(imageProvider);
                } catch (NoImageProviderException e) {
                }
                return context;
            }
        };

        // 图片解析
        htmlContext.setImageProvider(new AbstractImageProvider() {

//            String rootPath = Dumper.class.getResource("/").getPath();
            String rootPath = "D:/test";
            @Override
            public String getImageRootPath() {
                return rootPath;
            }

            @Override
            public Image retrieve(String src) {
                if (StringUtils.isEmpty(src)) {
                    return null;
                }
                try {
                    Image image = Image.getInstance(new File(rootPath, src).toURI().toString());
                    // 图片显示位置
                    image.setAbsolutePosition(400, 400);
                    store(src, image);
                    return image;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return super.retrieve(src);
            }
        });
        htmlContext.setAcceptUnknown(true).autoBookmark(true).setTagFactory(Tags.getHtmlTagProcessorFactory());

        // css解析
        CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(true);
        cssResolver.setFileRetrieve(new FileRetrieve() {
            @Override
            public void processFromStream(InputStream in,
                                          ReadingProcessor processor) throws IOException {
                try (
                        InputStreamReader reader = new InputStreamReader(in, charsetName)) {
                    int i;
                    while (-1 != (i = reader.read())) {
                        processor.process(i);
                    }
                } catch (Throwable e) {
                }
            }

            // 解析href
            @Override
            public void processFromHref(String href, ReadingProcessor processor) throws IOException {
//                InputStream is = Dumper.class.getResourceAsStream("/" + href);
                FileInputStream is = new FileInputStream("D:/test/" + href);
                try (InputStreamReader reader = new InputStreamReader(is,charsetName)) {
                    int i;
                    while (-1 != (i = reader.read())) {
                        processor.process(i);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        HtmlPipeline htmlPipeline = new HtmlPipeline(htmlContext, new PdfWriterPipeline(document, writer));
        Pipeline<?> pipeline = new CssResolverPipeline(cssResolver, htmlPipeline);
        XMLWorker worker = new XMLWorker(pipeline, true);
        XMLParser parser = new XMLParser(true, worker, Charset.forName(charsetName));
        try (InputStream inputStream = new ByteArrayInputStream(htmlStr.getBytes())) {
            parser.parse(inputStream, Charset.forName(charsetName));
        }
        document.close();
    }

}

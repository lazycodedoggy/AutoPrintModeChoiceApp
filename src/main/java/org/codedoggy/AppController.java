package org.codedoggy;

import com.aspose.words.Document;
import com.aspose.words.HeaderFooter;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.PageInfo;
import com.aspose.words.Section;
import com.aspose.words.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.PrinterName;

class AppController
{

    private static final String CONFIG_FILE_NAME = "config.properties";

    private static final String TEMP_DIRECTORY = "temp";

    private static final String configComment = "[forceModel]: [color] color mode, [no-color] black-white mode\n";

    private String rootPath;

    private Properties properties;

    private MainInterface mainInterface;

    static {
        try {
            Class<?> aClass = Class.forName("com.aspose.words.zzXyu");
            java.lang.reflect.Field zzYAC = aClass.getDeclaredField("zzZXG");
            zzYAC.setAccessible(true);

            java.lang.reflect.Field modifiersField = zzYAC.getClass().getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(zzYAC, zzYAC.getModifiers() & ~Modifier.FINAL);
            zzYAC.set(null, new byte[] { 76, 73, 67, 69, 78, 83, 69, 68 });
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    AppController(MainInterface mainInterface)
    {
        this.mainInterface = mainInterface;
        // read root path
        this.rootPath = System.getProperty("user.dir");

        this.properties = new Properties();
    }

    void smartPrint(String filePath, PrintService printService, Media portraitPaperBox, Media landscapePaperBox, int copies) throws Exception
    {
        // load the file
        Document doc = new Document(filePath);
        int pageCount = doc.getPageCount();
        Map<Document, AttributeSet> printJobs = new LinkedHashMap<>();
        for (int page = 0; page < pageCount; page++) {
            // get printer properties
            HashAttributeSet attributeSet = new HashAttributeSet();
            Document extractedPage = doc.extractPages(page, 1);
            PageInfo pageInfo = extractedPage.getPageInfo(0);
            boolean isLandscape = pageInfo.getLandscape();
            if (!isLandscape) {
                // remove the header and footer
                for (Section section : extractedPage.getSections()) {
                    for (HeaderFooter headerFooter : section.getHeadersFooters()) {
                        NodeCollection<Shape> shapes = headerFooter.getChildNodes(NodeType.SHAPE, true);
                        for (Shape shape : shapes) {
                            if (shape.hasImage()) {
                                shape.remove();
                            }
                        }
                    }
                }
            }

            String tempFilePath = getTempFilePath() + File.separator + doc.hashCode() + "_page_" + (page + 1) + ".docx";
            extractedPage.save(tempFilePath);
            Document extractedDoc = new Document(tempFilePath);
            boolean isColor = isLandscape || isColorImage(extractedDoc);
            // 检查配置
            String forceModel = (String) properties.get("forceModel");
            if (forceModel != null) {
                // 强制模式
                if (forceModel.equals("color")) {
                    isColor = true;
                }
                if (forceModel.equals("no-color")) {
                    isColor = false;
                }
            }

            mainInterface.printMessage(
                    "document page " + (page + 1) + (isLandscape ? " landscape " : " portrait ")
                            + ", " + (isColor ? "color mode" : "black-white mode")
                            + "\n");

            attributeSet.add(new PrinterName(printService.getName(), null));

            attributeSet.add(new Copies(1));

            Optional.ofNullable(isLandscape ? landscapePaperBox : portraitPaperBox).ifPresent(attributeSet::add);

            attributeSet.add(isColor ? Chromaticity.COLOR : Chromaticity.MONOCHROME);

            // extractedDoc.print(attributeSet);
            printJobs.put(extractedDoc, attributeSet);
        }

        mainInterface.printMessage("Processing completed, send the file to printer...\n");
        for (int i = 0; i < copies; i++) {
            for (Map.Entry<Document, AttributeSet> entry : printJobs.entrySet()) {
                entry.getKey().print(entry.getValue());
            }
        }
    }

    private String getTempFilePath()
    {
        return rootPath + File.separator + TEMP_DIRECTORY;
    }

    private boolean isColorImage(Document document) throws Exception
    {
        String tempPngFilePath = getTempFilePath() + File.separator + document.hashCode() + ".png";
        document.save(tempPngFilePath);

        BufferedImage bufferedImage = ImageIO.read(new File(tempPngFilePath));

        int[][][] result = null;
        int threshold = 20;

        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();
        result = new int[height][width][3];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = bufferedImage.getRGB(j, i);//visiting sequence for image: column, row
                result[i][j][0] = (rgb & 0xff0000) >> 16;
                result[i][j][1] = (rgb & 0xff00) >> 8;
                result[i][j][2] = (rgb & 0xff);

                if (Math.abs(result[i][j][0] - result[i][j][1]) <= threshold && Math.abs(result[i][j][1] - result[i][j][2]) <= threshold
                        && Math.abs(result[i][j][2] - result[i][j][0]) <= threshold) {

                } else {
                    return true;
                }
            }
        }

        return false;
    }

    Properties readConfig() throws IOException
    {
        String configPath = rootPath + File.separator + CONFIG_FILE_NAME;
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            configFile.createNewFile();
        }

        FileInputStream input = new FileInputStream(configFile);
        properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        return properties;
    }

    void saveConfig() throws IOException
    {
        String configPath = rootPath + File.separator + CONFIG_FILE_NAME;
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            configFile.createNewFile();
        }

        OutputStream os = Files.newOutputStream(configFile.toPath());
        OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        properties.store(osw, configComment);
    }

    void cleanTemp()
    {
        String tempPath = getTempFilePath();
        File tempDir = new File(tempPath);
        if (tempDir.exists()) {
            File[] files = tempDir.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

    void savePrinterState(PrintService printService, Media portraitPaperBox, Media landscapePaperBox)
    {
        properties.put("printService", printService.getName());
        properties.put("portraitPaperBox", String.valueOf(portraitPaperBox));
        properties.put("landscapePaperBox", String.valueOf(landscapePaperBox));
    }
}

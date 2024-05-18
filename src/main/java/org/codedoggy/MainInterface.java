package org.codedoggy;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaTray;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainInterface extends JFrame implements ActionListener
{
    private static final int WINDOW_WIDTH = 600;

    private static final int WINDOW_HEIGHT = 400;

    private AppController appController;

    private JButton fileChooserButton = new JButton("Choose File");

    private JLabel fileChooserTextLabel = new JLabel("No choice");

    private JComboBox<String> printChooserComboBox = new JComboBox<>();

    private JComboBox<String> portraitPaperBoxChooserComboBox = new JComboBox<>();

    private JComboBox<String> landscapePaperBoxChooserComboBox = new JComboBox<>();

    private JSpinner copiesSpinner = new JSpinner();

    private JButton printButton = new JButton("Printing");

    private JTextArea logTextArea;

    // printer list
    private PrintService[] printServices;

    // paper traies
    private Map<PrintService, List<Media>> printServiceToPaperBoxListMap = new HashMap<>();

    // current chosed printer
    private PrintService printService;

    // pre color printer tray (portrait)
    private Media portraitPaperBox;

    // normal tray
    private Media landscapePaperBox;

    private int copies = 1;

    private String filePath;

    private Properties properties;

    public MainInterface()
    {
        // set the title of window
        setTitle("AutoPrintModeChoiceApp");
        // set the size fo window
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        // center the window"
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        init();

        // choose files to print
        JPanel fileChooserPanel = new JPanel();
        fileChooserButton.addActionListener(this);

        JLabel fileChooserTitleLabel = new JLabel("Print：");
        fileChooserPanel.add(fileChooserTitleLabel);
        fileChooserPanel.add(fileChooserTextLabel);
        fileChooserPanel.add(fileChooserButton);

        // conf the printer
        JPanel printerChooserPanel = new JPanel();
        // choose printer
        initPrintChooserComboBox(printChooserComboBox);
        printChooserComboBox.addActionListener(this);
        // create a label for printer
        JLabel printerChooserTitleLabel = new JLabel("Printer：");
        printerChooserPanel.add(printerChooserTitleLabel);
        printerChooserPanel.add(printChooserComboBox);

        // conf the traies
        JPanel paperBoxPanel = new JPanel();

        JPanel portraitPaperChooserPanel = new JPanel();
        initPaperBoxChooserComboBox(portraitPaperBoxChooserComboBox);
        portraitPaperBoxChooserComboBox.addActionListener(this);
        JLabel portraitPaperBoxChooserTitleLabel = new JLabel("portrait paper tray: ");
        portraitPaperChooserPanel.add(portraitPaperBoxChooserTitleLabel);
        portraitPaperChooserPanel.add(portraitPaperBoxChooserComboBox);

        JPanel landscapePaperChooserPanel = new JPanel();
        initPaperBoxChooserComboBox(landscapePaperBoxChooserComboBox);
        landscapePaperBoxChooserComboBox.addActionListener(this);
        JLabel landscapePaperBoxChooserTitleLabel = new JLabel("landscape paper tray: ");
        landscapePaperChooserPanel.add(landscapePaperBoxChooserTitleLabel);
        landscapePaperChooserPanel.add(landscapePaperBoxChooserComboBox);

        paperBoxPanel.add(portraitPaperChooserPanel);
        paperBoxPanel.add(landscapePaperChooserPanel);

        JPanel copiesPanel = new JPanel();
        initCopiesValueSpinner(copiesSpinner);
        copiesSpinner.addChangeListener(e -> copies = (int) copiesSpinner.getValue());
        JLabel copiesTitleLabel = new JLabel("number of copies：");
        copiesPanel.add(copiesTitleLabel);
        copiesPanel.add(copiesSpinner);

        JPanel printPanel = new JPanel();
        printButton.addActionListener(this);
        printPanel.add(printButton);

        logTextArea = new JTextArea();
        JScrollPane textOutputScrollPane = new JScrollPane(logTextArea);
        textOutputScrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH, 100));
        textOutputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logTextArea.setEditable(false);
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        printMessage("Enviroment：\n" + "java.home: " + System.getProperty("java.home") + "\n" + "java.version: " + System.getProperty("java.version")
                + "\n");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(fileChooserPanel);
        mainPanel.add(printerChooserPanel);
        mainPanel.add(paperBoxPanel);
        mainPanel.add(copiesPanel);
        mainPanel.add(printPanel);
        mainPanel.add(textOutputScrollPane);

        add(mainPanel);

        recoverPrinterState();
    }

    private void recoverPrinterState()
    {
        if (properties == null) {
            return;
        }
        if (properties.get("printService") != null) {
            printChooserComboBox.setSelectedItem(properties.get("printService"));
        }
        if (properties.get("portraitPaperBox") != null) {
            portraitPaperBoxChooserComboBox.setSelectedItem(properties.get("portraitPaperBox"));
        }
        if (properties.get("landscapePaperBox") != null) {
            landscapePaperBoxChooserComboBox.setSelectedItem(properties.get("landscapePaperBox"));
        }
    }

    void printMessage(String message)
    {
        logTextArea.append(message);

        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());

        logTextArea.paintImmediately(logTextArea.getBounds());
    }

    private void initCopiesValueSpinner(JSpinner spinner)
    {
        spinner.setPreferredSize(new Dimension(50, 25));
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 100, 1);
        spinner.setModel(model);
    }

    private void init()
    {
        appController = new AppController(this);

        // get the printer list
        printServices = PrintServiceLookup.lookupPrintServices(null, null);
        if (printServices == null || printServices.length < 1) {
            JOptionPane.showMessageDialog(null, "Unable to retrieve printer information, plz contact the administrator");
        }
        // get printer traies list
        for (PrintService printService : printServices) {
            printServiceToPaperBoxListMap.put(printService, getPaperBoxListByPrintService(printService));
        }

        try {
            properties = appController.readConfig();
        } catch (Throwable e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Configuration error, please contact the administrator");
        }
    }

    private void initPaperBoxChooserComboBox(JComboBox<String> comboBox)
    {
        List<Media> paperBoxList = printServiceToPaperBoxListMap.get(printService);
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        if (!paperBoxList.isEmpty()) {
            portraitPaperBox = paperBoxList.get(0);
            landscapePaperBox = paperBoxList.get(0);
            for (Media mediaTray : paperBoxList) {
                model.addElement(String.valueOf(mediaTray));
            }
        }
        comboBox.setModel(model);
    }

    private List<Media> getPaperBoxListByPrintService(PrintService printService)
    {
        if (printService == null) {
            return null;
        }

        List<Media> mediaTrayList = new ArrayList<>();

        Object medias = printService.getSupportedAttributeValues(Media.class, null, null);

        if (medias instanceof Media[]) {
            Media[] trays = (Media[]) medias;

            System.out.println(printService.getName() + ": ");
            Arrays.stream(trays).forEach(tray -> System.out.println(tray.getClass() + " - " + tray));

            for (Media tray : trays) {
                if (tray instanceof MediaTray) {
                    mediaTrayList.add(tray);
                }
            }
        }
        return mediaTrayList;
    }

    private void initPrintChooserComboBox(JComboBox<String> comboBox)
    {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        if (properties != null && printServices.length > 0) {
            printService = printServices[0];
            for (PrintService service : printServices) {
                model.addElement(service.getName());
            }
        }
        comboBox.setModel(model);
    }

    public static void main(String[] args)
            throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {

        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        UIManager.setLookAndFeel(lookAndFeel);

        MainInterface mainInterface = new MainInterface();

        mainInterface.setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                mainInterface.shutdownHook();
            } catch (IOException e) {
                e.printStackTrace();
                mainInterface.printMessage(e.getMessage());
            }
        }));
    }

    private void shutdownHook() throws IOException
    {
        appController.savePrinterState(printService, portraitPaperBox, landscapePaperBox);
        appController.saveConfig();
        appController.cleanTemp();
    }

    private PrintService getPrintServiceByPrintName(String selectedPrintName)
    {
        for (PrintService printService : printServices) {
            if (printService.getName().equals(selectedPrintName)) {
                return printService;
            }
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        if (actionEvent.getSource() == fileChooserButton) {
            JFileChooser fileChooser = new JFileChooser();

            fileChooser.setFileFilter(new FileNameExtensionFilter("word files", "doc", "docx"));

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {

                File selectedFile = fileChooser.getSelectedFile();

                if (!selectedFile.getName().endsWith(".doc") && !selectedFile.getName().endsWith(".docx")) {
                    JOptionPane.showMessageDialog(null, "Please select a file in .doc or .docx format");
                    return;
                }
                filePath = selectedFile.getAbsolutePath();

                fileChooserTextLabel.setText(filePath);
            }
        } else if (actionEvent.getSource() == printChooserComboBox) {
            String selectedPrintName = (String) printChooserComboBox.getSelectedItem();
            printService = getPrintServiceByPrintName(selectedPrintName);

            clearPaperBoxMediaTray();
            initPaperBoxChooserComboBox(portraitPaperBoxChooserComboBox);
            initPaperBoxChooserComboBox(landscapePaperBoxChooserComboBox);
        } else if (actionEvent.getSource() == portraitPaperBoxChooserComboBox) {
            String selectedportraitPaperBoxName = (String) portraitPaperBoxChooserComboBox.getSelectedItem();
            portraitPaperBox = getPaperBoxByName(selectedportraitPaperBoxName);
        } else if (actionEvent.getSource() == landscapePaperBoxChooserComboBox) {
            String selectedlandscapePaperBoxName = (String) landscapePaperBoxChooserComboBox.getSelectedItem();
            landscapePaperBox = getPaperBoxByName(selectedlandscapePaperBoxName);
        } else if (actionEvent.getSource() == printButton) {
            if (filePath == null) {
                JOptionPane.showMessageDialog(null, "select files");
                return;
            }
            if (printService == null) {
                JOptionPane.showMessageDialog(null, "select printers");
                return;
            }

            printButton.setEnabled(false);
            SwingUtilities.invokeLater(() -> {
                try {
                    printMessage("start parse file...\n");
                    appController.smartPrint(filePath, printService, portraitPaperBox, landscapePaperBox, copies);
                    printMessage("The print job has been sent to the printer, this print task is now complete....\n");
                } catch (Throwable exception) {
                    printMessage("error in parsing...\n");
                    exception.printStackTrace();
                    printMessage(exception.getMessage() + "\n");
                    printMessage(Arrays.toString(exception.getStackTrace()) + "\n");
                }
                SwingUtilities.invokeLater(() -> {
                    // 等待1秒
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    printButton.setEnabled(true);
                });
            });
        }
        System.out.println(this);
    }

    private void clearPaperBoxMediaTray()
    {
        portraitPaperBox = null;
        landscapePaperBox = null;
    }

    private Media getPaperBoxByName(String name)
    {
        List<Media> mediaTrays = printServiceToPaperBoxListMap.get(printService);
        for (Media mediaTray : mediaTrays) {
            if (String.valueOf(mediaTray).equals(name)) {
                return mediaTray;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return "MainInterface{" + "printService=" + printService + ", portraitPaperBox=" + portraitPaperBox + ", landscapePaperBox="
                + landscapePaperBox + ", copies=" + copies + '}';
    }
}


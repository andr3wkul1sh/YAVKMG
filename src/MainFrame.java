import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class MainFrame extends JFrame {

    private JButton parseButton;
    private JTextField saveTo;
    private JTextField url;
    private JTextArea log;
    private JPanel downloadLinksPanel;
    private ParsedList parsedList;

    public MainFrame() {
        initComponents();
    }

    private void initComponents() {
        setSize(new Dimension(500, 300));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        url = new JTextField();
        TextPrompt urlTextPrompt = new TextPrompt("Input URL or search phrase", url, TextPrompt.Show.ALWAYS);
        urlTextPrompt.changeAlpha(128);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.8;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(url, c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.2;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        parseButton = new JButton("Get");
        parseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parsedList = new ParsedList();
                new Thread() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                parse();
                                makeLinks();
                                downloadLinksPanel.revalidate();
                            }
                        });
                    }
                }.start();
            }
        });
        add(parseButton, c);

        saveTo = new JTextField();
        TextPrompt saveToTextPrompt = new TextPrompt("Input path to save", saveTo, TextPrompt.Show.ALWAYS);
        saveToTextPrompt.changeAlpha(128);
        saveTo.setText("D:/");
        saveTo.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser(".");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fileChooser.showSaveDialog(saveTo);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File dirFile = fileChooser.getSelectedFile();
                    saveTo.setText(dirFile.getAbsolutePath());
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(saveTo, c);

        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.weighty = 0.3;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        log = new JTextArea();
        JScrollPane scroll = new JScrollPane(log);
        add(scroll, c);

        downloadLinksPanel = new JPanel();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0.7;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPanel = new JScrollPane(downloadLinksPanel);
        add(scrollPanel, c);
    }

    public void makeLinks() {
        downloadLinksPanel.removeAll();
        downloadLinksPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        for (int i = 0; i < parsedList.size(); i++) {
            final ParsedListElement element = parsedList.getElement(i);
            final JCheckBox flag = new JCheckBox("", element.isDownloadFlag());
            flag.setName(Integer.toString(i));
            flag.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    ParsedListElement element = parsedList.getElement(Integer.parseInt(flag.getName()));
                    element.setDownloadFlag(flag.isSelected());
                }
            });
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0.2;
            c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            downloadLinksPanel.add(flag, c);

            JLabel name = new JLabel();
            name.setText(element.getAuthor() + " - " + element.getSong());
            c.weightx = 0.6;
            c.weighty = 0;
            c.gridx = 1;
            c.gridy = i;
            c.fill = GridBagConstraints.HORIZONTAL;
            downloadLinksPanel.add(name, c);

            final JLabel download = new JLabel();
            download.setText("Download");
            download.setName(Integer.toString(i));
            download.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ParsedListElement element = parsedList.getElement(Integer.parseInt(download.getName()));
                    try {
                        saveSong(element.getURL(), element.getAuthor(), element.getSong());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    download.setForeground(Color.red);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    download.setForeground(Color.black);
                }
            });
            c.weightx = 0.2;
            c.weighty = 0;
            c.gridx = 2;
            c.gridy = i;
            c.fill = GridBagConstraints.HORIZONTAL;
            downloadLinksPanel.add(download, c);

        }
        final JLabel downloadSelected = new JLabel();
        downloadSelected.setText("Download selected");
        downloadSelected.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < parsedList.size(); i++) {
                    ParsedListElement element = parsedList.getElement(i);
                    if (element.isDownloadFlag()) {
                        try {
                            saveSong(element.getURL(), element.getAuthor(), element.getSong());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                downloadSelected.setForeground(Color.red);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                downloadSelected.setForeground(Color.black);
            }
        });
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = parsedList.size();
        c.fill = GridBagConstraints.HORIZONTAL;
        downloadLinksPanel.add(downloadSelected, c);
    }

    private void parse() {
        String urlToParse = url.getText().trim();
        try {
            URL testURL = new URL(urlToParse);
            log.setText(log.getText() + "URL valid " + testURL.toString() + "\n");
        } catch (MalformedURLException e) {
            log.setText(log.getText() + "Invalid URL!\n");
            return;
        }
        try {
            final WebClient webClient = new WebClient(BrowserVersion.getDefault());
            //page to parse ("http://vk.com/wall-25993158_3791")
            final HtmlPage page = webClient.getPage(urlToParse);
            //webClient.waitForBackgroundJavaScript(5000);
            List<DomElement> inputs = page.getElementsByTagName("input");
            for (DomElement domy : inputs) {
                if (domy.getAttribute("id").startsWith("audio")) {
                    String songName = "", songAuthor = "";
                    String id = domy.getAttribute("id").substring(10, domy.getAttribute("id").length());
                    HtmlDivision divy = page.getHtmlElementById("audio" + id);
                    List<HtmlElement> spans = divy.getHtmlElementsByTagName("span");
                    for (HtmlElement spany : spans) {
                        if (spany.getAttribute("id").startsWith("title")) {
                            songName = spany.getTextContent();
                        }
                    }
                    List<HtmlElement> aTags = divy.getHtmlElementsByTagName("a");
                    for (HtmlElement aTag : aTags) {
                        if (aTag.hasAttribute("href")) {
                            songAuthor = aTag.getTextContent();
                        }
                    }
                    int urlStartPos = domy.asXml().indexOf("http://");
                    int urlEndPos = domy.asXml().indexOf(".mp3,");
                    String urlMp3 = domy.asXml().substring(urlStartPos, urlEndPos + 4);
                    ParsedListElement element = new ParsedListElement(songAuthor, songName, urlMp3);
                    parsedList.addElement(element);
                }

            }
            webClient.closeAllWindows();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void saveSong(String urlString, String author, String name) throws IOException {
        long startTime = System.currentTimeMillis();
        log.setText(log.getText() + "Connecting to VK site...\n");
        URL url = new URL(urlString);
        url.openConnection();
        InputStream reader = url.openStream();
  
        /*
         * Setup a buffered file writer to write
         * out what we read from the website.
         */
        FileOutputStream writer = new FileOutputStream(saveTo.getText() + author + " - " + name + ".mp3");
        byte[] buffer = new byte[153600];
        int totalBytesRead = 0;
        int bytesRead;
        log.setText(log.getText() + "Reading file 150KB blocks at a time.\n");

        while ((bytesRead = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, bytesRead);
            buffer = new byte[153600];
            totalBytesRead += bytesRead;
        }

        long endTime = System.currentTimeMillis();

        log.setText(log.getText() + "Done. " + totalBytesRead + " bytes read (" + (endTime - startTime) + " millseconds).\n");
        writer.close();
        reader.close();
    }
}

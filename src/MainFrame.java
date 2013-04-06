import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.swing.*;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class MainFrame extends JFrame{

    private JButton parseButton;
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
        url.setName("test");
		c.weightx = 0.8;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(url,c);

		c.gridx = 1;
		c.weightx = 0.2;
		c.weighty = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
        parseButton = new JButton("Get");
        parseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //
                parsedList = new ParsedList();
                //ParsedListElement element = new ParsedListElement("name", "song", "URL");
                //parsedList.addElement(element);
                new Thread() {
                    @Override public void run () {
                        SwingUtilities.invokeLater(new Runnable(){
                            @Override public void run() {
                                parse();
                                makeLinks();
                                downloadLinksPanel.revalidate();
                            }
                        });
                    }
                }.start();
            }
        });
		add(parseButton,c);

		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 0.3;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
        log = new JTextArea();
        JScrollPane scroll = new JScrollPane(log);
		add(scroll,c);

        downloadLinksPanel = new JPanel();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0.7;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPanel = new JScrollPane(downloadLinksPanel);
        add(scrollPanel,c);
	}

    public void makeLinks() {
        downloadLinksPanel.removeAll();
        downloadLinksPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        for (int i = 0; i < parsedList.size(); i++) {
            ParsedListElement element = parsedList.getElement(i);
            JCheckBox flag = new JCheckBox("", element.isDownloadFlag());
            c.weightx = 0.2;
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = i;
            downloadLinksPanel.add(flag,c);

            JLabel name = new JLabel();
            name.setText(element.getAuthor() + " - " + element.getSong());
            c.weightx = 0.6;
            c.weighty = 0;
            c.gridx = 1;
            c.gridy = i;
            c.fill = GridBagConstraints.HORIZONTAL;
            downloadLinksPanel.add(name,c);

            JLabel download = new JLabel();
            download.setText("Download");
            c.weightx = 0.2;
            c.weighty = 0;
            c.gridx = 2;
            c.gridy = i;
            c.fill = GridBagConstraints.HORIZONTAL;
            downloadLinksPanel.add(download,c);

        }
    }

    private void parse() {
        try {
            final WebClient webClient = new WebClient(BrowserVersion.getDefault());
            //page to parse ("http://vk.com/wall-25993158_3791")
            final HtmlPage page = webClient.getPage(url.getText());
            webClient.waitForBackgroundJavaScript(1000);
            List<DomElement> inputs = page.getElementsByTagName("input");
            for (DomElement domy : inputs) {
                if (domy.getAttribute("id").startsWith("audio")) {
                    String songName="", songAuthor="";
                    String id = domy.getAttribute("id").substring(10, domy.getAttribute("id").length());
                    HtmlDivision divy = page.getHtmlElementById("audio"+ id);
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
                    log.setText(log.getText() + songAuthor + " # " + songName + "\n");
                    int urlStartPos = domy.asXml().indexOf("http://");
                    int urlEndPos = domy.asXml().indexOf(".mp3,");
                    String urlMp3 = domy.asXml().substring(urlStartPos, urlEndPos + 4);
                    //saveSong(urlMp3, songAuthor, songName);
                    //Thread.sleep(4000);
                    ParsedListElement element = new ParsedListElement(songAuthor, songName, urlMp3);
                    parsedList.addElement(element);
                }

            }
            webClient.closeAllWindows();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private static void saveSong(String urly, String author, String name) throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("Connecting to VK site...\n");
        URL url = new URL(urly);
        url.openConnection();
        InputStream reader = url.openStream();
  
        /*
         * Setup a buffered file writer to write
         * out what we read from the website.
         */
        FileOutputStream writer = new FileOutputStream("D:/vk/" + author + " - " + name + ".mp3");
        byte[] buffer = new byte[153600];
        int totalBytesRead = 0;
        int bytesRead;
  
        System.out.println("Reading file 150KB blocks at a time.\n");
  
        while ((bytesRead = reader.read(buffer)) > 0)
        { 
           writer.write(buffer, 0, bytesRead);
           buffer = new byte[153600];
           totalBytesRead += bytesRead;
        }
  
        long endTime = System.currentTimeMillis();
  
        System.out.println("Done. " + totalBytesRead + " bytes read (" + (endTime - startTime) + " millseconds).\n");
        writer.close();
        reader.close();
	}
}

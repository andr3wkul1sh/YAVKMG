public class ParsedListElement {
    private String author;
    private String song;
    private String URL;
    private boolean downloadFlag;

    public ParsedListElement() {
        author = "";
        song = "";
        URL = "";
        downloadFlag = false;
    }

    public ParsedListElement(String author, String song, String URL) {
        this.author = author;
        this.song = song;
        this.URL = URL;
        downloadFlag = false;
    }

    public void setDownloadFlag(boolean flag) {
        downloadFlag = flag;
    }

    public boolean isDownloadFlag() {
        return downloadFlag;
    }

    public String getAuthor() {
        return author;
    }

    public String getSong() {
        return song;
    }

    public String getURL() {
        return URL;
    }
}

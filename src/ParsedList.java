import java.util.ArrayList;

public class ParsedList{

    private ArrayList<ParsedListElement> list;

    public ParsedList() {
        list = new ArrayList<ParsedListElement>();
    }

    public void addElement(ParsedListElement element) {
        list.add(element);
    }

    public ParsedListElement getElement(int i) {
        return list.get(i);
    }

    public int size() {
        return list.size();
    }
}

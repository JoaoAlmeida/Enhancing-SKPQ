package lodweb;

import javax.faces.bean.ManagedBean;

@ManagedBean
public class ValidationView {
     
    private String text;

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}

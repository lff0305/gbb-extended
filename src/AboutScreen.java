import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.SeparatorField;

public final class AboutScreen extends TransitionScreen {
	public AboutScreen(){
		super(true, VERTICAL_SCROLL | VERTICAL_SCROLLBAR);                

		setTitle("GBBReader - 关于");
        
		add(new NullField(FOCUSABLE)); // 为了让内容能滚动
        Font _font = (this.getFont()).derive(Font.BOLD);
        LabelField _name = new LabelField("GBBReader 1.0.2",FIELD_HCENTER | FIELD_VCENTER | NON_FOCUSABLE);
        _name.setFont( _font);
        add(_name);
        
        add(new LabelField("黑莓手机文本阅读",FIELD_HCENTER | FIELD_VCENTER | NON_FOCUSABLE));
        add(new SeparatorField());
        LabelField _author = new LabelField("作者：wick",FIELD_HCENTER | FIELD_VCENTER | NON_FOCUSABLE);
        _author.setFont(_font);
        add(_author);
        add(new SeparatorField());
        add(new LabelField("本程序基于GPL 2.0版",FIELD_HCENTER | FIELD_VCENTER | NON_FOCUSABLE));
        add(new SeparatorField());
        add(new LabelField("联系：loubingyong@gmail.com",FIELD_HCENTER | FIELD_VCENTER | NON_FOCUSABLE));
		add(new NullField(FOCUSABLE)); // 为了让内容能滚动
	}
	
}

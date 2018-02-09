package org.celllife.mobilisr.client.view.gxt;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;

public class DirtyCheckFormButtonBinding extends FormButtonBinding {

	private List<Button> buttons;
	private final FormPanel panel;
	private final boolean includeDirtyState;

	public DirtyCheckFormButtonBinding(FormPanel panel, boolean includeDirtyState) {
		super(panel);
		this.panel = panel;
		this.includeDirtyState = includeDirtyState;
		buttons = new ArrayList<Button>();
	}

	@Override
	public void addButton(Button button) {
		buttons.add(button);
	}

	@Override
	public void removeButton(Button button) {
		buttons.remove(button);
	}

	@Override
	protected boolean checkPanel() {
		boolean v = panel.isValid(true);
		boolean d = includeDirtyState ? panel.isDirty() : true;
		boolean vd = v&&d;
		for (Button button : buttons) {
			if (vd != button.isEnabled()) {
				button.setEnabled(vd);
			}
		}
		return v;
	}
}

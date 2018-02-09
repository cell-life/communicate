/*
 * Ext GWT 2.2.4 - Ext for GWT
 * Copyright(c) 2007-2010, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout;

/**
 * <code>CenterTopLayout</code> centers a single widget at the top of its container.
 */
public class CenterTopLayout extends AbsoluteLayout {

  public CenterTopLayout() {
    monitorResize = true;
  }

  @Override
  protected void onLayout(Container<?> container, El target) {
    super.onLayout(container, target);
    Component c = container.getItem(0);
    if (c != null) {
      callLayout(c, false);

      Point p = c.el().getAlignToXY(target.dom, "t-t", null);
      p = c.el().translatePoints(p);
      setPosition(c, p.x, p.y);
    }
  }
}

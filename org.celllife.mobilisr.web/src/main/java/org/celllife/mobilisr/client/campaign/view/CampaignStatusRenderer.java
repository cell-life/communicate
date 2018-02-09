package org.celllife.mobilisr.client.campaign.view;

import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.domain.Campaign;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

public class CampaignStatusRenderer implements
			GridCellRenderer<BeanModel> {
		private final DateTimeFormat format = DateTimeFormat.getFormat("dd-MM-yyyy");

		@Override
		public Object render(BeanModel model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<BeanModel> store, Grid<BeanModel> grid) {
			
			Campaign campaign = getCampaign(model);
			CampaignStatus status = campaign.getStatus();

			String htmlImg = status.toString();
			if (campaign.isActive()){
				htmlImg += " (since " + format.format(campaign.getStartDate()) + ")";
			} else if (CampaignStatus.STOPPING.equals(status)
					|| CampaignStatus.FINISHED.equals(status)){
				htmlImg += " (since " + format.format(campaign.getEndDate()) + ")";
			}
			ImageResource image = getImage(status);
			
			LayoutContainer container = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
			container.add(new Image(image), new RowData());
			container.add(new Label(htmlImg), new RowData(-1,-1, new Margins(0,0,0,5)));
			// hack to get it to display since the layout() method is not called
			// after it is added to the cell
			container.setHeight(32);
			return container;
		}
		
		private ImageResource getImage(CampaignStatus status) {
			switch(status){
			case ACTIVE:
				return Resources.INSTANCE.active();
			case INACTIVE:
				return Resources.INSTANCE.inactive();
			case RUNNING:
				return Resources.INSTANCE.running();
			case FINISHED:
				return Resources.INSTANCE.finished();
			case SCHEDULE_ERROR:
				return Resources.INSTANCE.scheduleError();
			case SCHEDULED:
				return Resources.INSTANCE.schedule();
			case STOPPING:
				return Resources.INSTANCE.stopping();
			}
			return Resources.INSTANCE.blank();
		}

		/**
		 * Override this method if the BeanModel type is not wrapping a Campaign.
		 * 
		 * @param model
		 * @return
		 */
		protected Campaign getCampaign(BeanModel model){
			return model.getBean();
		}
	}
package org.celllife.mobilisr.client.app.presenter;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.app.FooterView;
import org.celllife.mobilisr.client.app.MobilisrEventBus;
import org.celllife.mobilisr.client.app.view.FooterViewImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.LazyPresenter;

@Presenter(view = FooterViewImpl.class)
public class FooterPresenter extends
		LazyPresenter<FooterView, MobilisrEventBus> {

	private static final String UNKNOWN_VERSION = "Unknown version";
	private String version;
	
	@Override
	public void bindView() {
		getView().getWhatsNewAnchor().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				getEventBus().showWhatsNew();
			}
		});
	}

	public void onStart() {
		eventBus.setRegionFooter(getView().getViewWidget());

		getVersion();
	}

	protected void getVersion() {
		if (version != null){
			setFooterText(version);
		}
		
		String hostPageBaseURL = GWT.getHostPageBaseURL();
		if (!hostPageBaseURL.endsWith("/")) {
			hostPageBaseURL = hostPageBaseURL + "/";
		}
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET,
				hostPageBaseURL + "mobilisr/version");
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					version = UNKNOWN_VERSION;
				}

				public void onResponseReceived(Request request,
						Response response) {
					version = response.getText();
					setFooterText(version);
				}
			});
		} catch (RequestException ex) {
			version = UNKNOWN_VERSION;
		}

	}

	protected void setFooterText(String version) {
		getView().getFooterLabel().setText(Messages.INSTANCE.footerLabel(version));
	}
}

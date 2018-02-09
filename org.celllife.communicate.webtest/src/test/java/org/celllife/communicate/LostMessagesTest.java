package org.celllife.communicate;

import junit.framework.Assert;

import org.celllife.communicate.page.LoggedInPage;
import org.celllife.communicate.page.LoginPage;
import org.celllife.communicate.page.LostMessagesPage;
import org.celllife.communicate.util.AbstractBaseTest;
import org.celllife.mobilisr.domain.SmsLog;
import org.gwttime.time.DateTime;
import org.junit.Test;

public class LostMessagesTest extends AbstractBaseTest {

	public LostMessagesPage goLostMessages(){
		LoginPage lp = getLogin();
		LoggedInPage hp = lp.loginAsAdmin();
		return hp.goAdminPage().goLostMessagesPage();
	}

	@Test
	public void testSelectAllDelete() {
		int iNumMessages = 3;
		for (int i=0; i<iNumMessages; i++) {
			db().createTestEntity(SmsLog.class);
		}
		LostMessagesPage lmp = goLostMessages();
		Assert.assertEquals("Displaying 1 - " + iNumMessages + " of " + iNumMessages,
				lmp.getPagingText());
		lmp.selectAllMessages();
		lmp.deleteAndConfirm(iNumMessages);
		Assert.assertEquals("No data to display", lmp.getPagingText());
		Assert.assertEquals("Messages voided: " + iNumMessages, lmp.getSuccessMessageText());
	}

	@Test
	public void testReprocess() {
		db().createTestEntity(SmsLog.class);
		LostMessagesPage lmp = goLostMessages();
		Assert.assertEquals("Displaying 1 - 1 of 1", lmp.getPagingText());
		lmp.selectAllMessages();
		lmp.clickReprocess();
		Assert.assertEquals("Messages queued for re-processing: 1", lmp.getSuccessMessageText());
	}


	@Test
	public void testSearchMsisdn() {
		// Create one message.
		SmsLog target = db().createTestEntity(SmsLog.class);
		// set targets date in the past to force it to be at the end of the list
		target.setDatetime(new DateTime().minusDays(1).toDate());
		db().saveOrgUpdate(target);
		String targetNumber = target.getMsisdn();

		// Create a pageful of messages (page holds 75 messages -- BR270I )
		// (Should push target message to to next page).
		int iNumMessages = 75;
		for (int i=0; i<iNumMessages; i++) {
			db().createTestEntity(SmsLog.class);
		}

		// Confirm number of messages
		LostMessagesPage lmp = goLostMessages();
		Assert.assertEquals("Displaying 1 - " + iNumMessages + " of " + (iNumMessages + 1),
				lmp.getPagingText());
		// Confirm the target message is not visible on this page.
		Assert.assertFalse(lmp.isMessageOnPage(target.getId()));

		lmp.setSearchText(targetNumber);
		Assert.assertEquals("Displaying 1 - 1 of 1", lmp.getPagingText());
		Assert.assertTrue(lmp.isMessageOnPage(target.getId()));
	}
}

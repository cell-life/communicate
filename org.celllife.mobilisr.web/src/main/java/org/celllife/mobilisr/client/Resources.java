package org.celllife.mobilisr.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface Resources extends ClientBundle {
  public static final Resources INSTANCE =  GWT.create(Resources.class);

  @Source("images/status/ACTIVE.png")
  public ImageResource active();
  
  @Source("images/add.png")
  public ImageResource add();
  
  @Source("images/add_grey.png")
  public ImageResource addGrey();

  @Source("images/addressbook.png")
  public ImageResource addressbook();
 
  @Source("images/admin.png")
  public ImageResource admin();
  
  @Source("images/application-form.png")
  public ImageResource applicationForm();

  @Source("images/ext-ux-wiz-stepIndicator-off.png")
  public ImageResource blank();
  
  @Source("images/chameleon_green.png")
  public ImageResource chameleonGreen();
  
  @Source("images/chameleon_red.png")
  public ImageResource chameleonRed();
  
  @Source("images/clear-trigger.png")
  public ImageResource clearTrigger();
  
  @Source("images/credit.png")
  public ImageResource credit();
  
  @Source("images/cog.png")
  public ImageResource cog();

  @Source("images/csv.png")
  public ImageResource csv();
  
  @Source("images/delete.png")
  public ImageResource delete();
  
  @Source("images/delete-grey.png")
  public ImageResource deleteGrey();
  
  @Source("images/download.png")
  public ImageResource download();
  
  @Source("images/error.png")
  public ImageResource error();
  
  @Source("images/status/FINISHED.png")
  public ImageResource finished();

  @Source("images/smsstatus/flag-blue.png")
  public ImageResource flagBlue();
  
  @Source("images/smsstatus/flag-green.png")
  public ImageResource flagGreen();
  
  @Source("images/smsstatus/flag-red.png")
  public ImageResource flagRed();
  
  @Source("images/smsstatus/flag-yellow.png")
  public ImageResource flagYellow();
  
  @Source("images/folder-clock.png")
  public ImageResource folderClock();
  
  @Source("images/folder-table.png")
  public ImageResource folderTable();
  
  @Source("images/home.png")
  public ImageResource home();
  
  @Source("images/status/INACTIVE.png")
  public ImageResource inactive();
  
  @Source("images/incoming.png")
  public ImageResource incoming();
  
  @Source("images/information.png")
  public ImageResource information();
  
  @Source("images/list-add-3.png")
  public ImageResource listAdd();
  
  @Source("images/list-remove-3.png")
  public ImageResource listRemove();
  
  @Source("images/mail-send.png")
  public ImageResource mailSend();
  
  @Source("images/manage-recipients.png")
  public ImageResource manageRecipients();
  
  @Source("images/email-edit.png")
  public ImageResource messageEdit();

  @Source("images/message-logs.png")
  public ImageResource messageLogs();

  @Source("images/email-go.png")
  public ImageResource messageTest();
  
  @Source("images/outgoing.png")
  public ImageResource outgoing();
  
  @Source("images/arrow-refresh.png")
  public ImageResource refresh();

  @Source("images/routing.png")
  public ImageResource routing();
  
  @Source("images/status/RUNNING.png")
  public ImageResource running();
  
  @Source("images/status/SCHEDULED.png")
  public ImageResource schedule();
  
  @Source("images/status/SCHEDULE_ERROR.png")
  public ImageResource scheduleError();
  
  @Source("images/folder-clock.png")
  public ImageResource scheduleList();
  
  @Source("images/status/SCHEDULED_NoStop.png")
  public ImageResource scheduleNoStop();
  
  @Source("images/status/SCHEDULED_NoStop_grey.png")
  public ImageResource scheduleNoStopGrey();
  
  @Source("images/scheduled_pause.png")
  public ImageResource schedulePause();
  
  @Source("images/start.png")
  public ImageResource start();
  
  @Source("images/stop.png")
  public ImageResource stop();
  
  @Source("images/status/STOPPING.png")
  public ImageResource stopping();
  
  @Source("images/success.png")
  public ImageResource success();
  
  @Source("images/summary.png")
  public ImageResource summary();
  
  @Source("images/table-edit.png")
  public ImageResource tableEdit();
  
  @Source("images/edit-delete-2.png")
  public ImageResource trash();
  
  @Source("images/view-recipients.png")
  public ImageResource viewRecipients();
  
  @Source("images/ext-ux-wiz-stepIndicator-off.png")
  public ImageResource wizardStepOff();
  
  @Source("images/ext-ux-wiz-stepIndicator-on.png")
  public ImageResource wizardStepOn();
  
}
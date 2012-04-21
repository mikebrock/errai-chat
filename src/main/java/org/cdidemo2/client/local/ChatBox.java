package org.cdidemo2.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.Widget;
import org.cdidemo2.client.shared.ChatMessage;
import org.cdidemo2.client.shared.Client;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Date;

/**
 * @author Mike Brock
 */
public class ChatBox extends Composite {
  @Inject UiBinder<Widget, ChatBox> uiBinder;

  @UiField Tree loggedIn;
  @UiField Label statusSummary;
  @UiField SimplePanel chatBox;
  @UiField TextBox textEntry;
  @UiField Button sendMessage;

  @Inject @Client Event<ChatMessage> chatMessageEvent;
  @Inject SessionData sessionData;

  @PostConstruct
  private void buildWidget() {
    initWidget(uiBinder.createAndBindUi(this));

    textEntry.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          onClick(null);
        }
      }
    });
  }

  public void addMessage(final String nick, final String message) {
    Element chatElement = new HTML("<strong>" + nick + "</strong>: " + message + "<br/>").getElement();
    chatBox.getElement().appendChild(chatElement);
    chatBox.getElement().setScrollTop(chatElement.getOffsetTop());
  }

  public void addEvent(final String nick, final String eventString) {
    chatBox.getElement().appendChild(new HTML("<span style='color:green;'> *** <strong>" + nick + "</strong> "
            + eventString + "</span>").getElement());
  }

  public void addLoggedInUser(final String nick) {
    HTML html = new HTML("<span><i class='icon-search icon-user'></i>&nbsp;" + nick + "</span>");
    loggedIn.add(html);
    updateStatus();
  }

  public void clearLoggedInUsers() {
    loggedIn.clear();
  }

  @UiHandler("sendMessage")
  public void onClick(ClickEvent event) {
    if (textEntry.getText().trim().length() > 0) {
      chatMessageEvent.fire(new ChatMessage(new Date(), sessionData.getNickName(), textEntry.getText()));
      textEntry.setText("");
    }
  }

  public void calculateHeight() {
    chatBox.setHeight((Window.getClientHeight() - 45) + "px");
  }

  private void updateStatus() {
    statusSummary.setText("Logged in users: " + sessionData.getLoggedInUsers().size());
  }
}

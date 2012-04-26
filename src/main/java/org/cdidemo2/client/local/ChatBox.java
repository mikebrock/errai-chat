package org.cdidemo2.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
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
import org.cdidemo2.client.shared.Action;
import org.cdidemo2.client.shared.ActionMessage;
import org.cdidemo2.client.shared.AdminAction;
import org.cdidemo2.client.shared.AdminAuth;
import org.cdidemo2.client.shared.ChatMessage;
import org.cdidemo2.client.shared.Client;
import org.jboss.errai.common.client.util.LogUtil;
import sun.net.idn.StringPrep;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Date;

import static com.google.gwt.safehtml.shared.SimpleHtmlSanitizer.sanitizeHtml;

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
  @Inject @Client Event<ActionMessage> actionMessageEvent;
  @Inject Event<AdminAuth> adminAuthEvent;
  @Inject Event<AdminAction> adminActionEvent;

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
    if (sessionData.getNickName().equals(nick)) return;

    if (message.contains(sessionData.getNickName())) {
      if (!Notifications.hasFocus()) {
        Notifications.notify(nick + " mentioned you", message);
      }

      appendItem(new HTML("<div style='background-color: #eaeaea; width:100%;'><strong>"
              + sanitizeHtml(nick).asString() + "</strong>: "
              + sanitizeHtml(message).asString() + "</div>").getElement());
    }
    else {
      appendItem(new HTML("<strong>" + sanitizeHtml(nick).asString() + "</strong>: "
              + sanitizeHtml(message).asString()
              + "<br/>").getElement());
    }
  }

  public void addActionMessage(final String nick, final String message) {
    if (sessionData.getNickName().equals(nick)) return;

    if (message.contains(sessionData.getNickName())) {
      if (!Notifications.hasFocus()) {
        Notifications.notify(nick + " mentioned you", message);
      }

      appendItem(new HTML("<div style='background-color: #eaeaea; width:100%;'> * "
              + sanitizeHtml(nick).asString() + " "
              + sanitizeHtml(message).asString() + "</div>").getElement());
    }
    else {
      appendItem(new HTML(" * " + sanitizeHtml(nick).asString() + " "
              + sanitizeHtml(message).asString()
              + "<br/>").getElement());
    }
  }

  public void addEvent(final String nick, final String eventString) {
    appendItem(new HTML("<span style='color:green;'> *** <strong>" + sanitizeHtml(nick).asString() + "</strong> "
            + sanitizeHtml(eventString).asString() + "</span>").getElement());
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
    final String text = textEntry.getText().trim();
    if (text.length() > 0) {
      if (text.startsWith("/me ")) {
        String actionMessage = text.substring("/me ".length());
        actionMessageEvent.fire(new ActionMessage(new Date(), sessionData.getNickName(), actionMessage));
        appendItem(new HTML("<span style='color:blue'> * " + sessionData.getNickName() + " "
                + sanitizeHtml(actionMessage).asString() + "</span><br/>").getElement());
      }
      else if (text.startsWith("/auth ")) {
        String[] commandParts = text.split(" ");
        if (expectParms(1, commandParts)) {
          adminAuthEvent.fire(new AdminAuth(commandParts[1]));
        }
      }
      else if (text.startsWith("/op ")) {
        String[] commandParts = text.split(" ");
        if (expectParms(1, commandParts)) {
         adminActionEvent.fire(new AdminAction(Action.Op, commandParts[1]));
        }
      }
      else if (text.startsWith("/deop ")){
        String[] commandParts = text.split(" ");
        if (expectParms(1, commandParts)) {
         adminActionEvent.fire(new AdminAction(Action.Deop, commandParts[1]));
        }
      }
      else if (text.startsWith("/kick ")){
        String[] commandParts = text.split(" ");
        if (expectParms(1, commandParts)) {
         adminActionEvent.fire(new AdminAction(Action.Kick, commandParts[1]));
        }
      }
      else if (text.startsWith("/topic ")){
         adminActionEvent.fire(new AdminAction(Action.Topic, text.substring("/topic ".length())));
      }
      else {
        chatMessageEvent.fire(new ChatMessage(new Date(), sessionData.getNickName(), text));
        appendItem(new HTML("<span style='color:blue'><strong>"
                + sessionData.getNickName() + "</strong>: " +
                sanitizeHtml(text).asString() + "</span><br/>").getElement());
      }
      textEntry.setText("");
    }
  }

  private static boolean expectParms(int num, String[] parms) {
    if (num + 1 != parms.length) {
      Window.alert("Incorrect number of parameters (Expected: " + num + ")");

      return false;
    }
    else {
      return true;
    }
  }

  public void calculateHeight() {
    chatBox.setHeight((Window.getClientHeight() - 45) + "px");
  }

  private void updateStatus() {
    statusSummary.setText("Logged in users: " + sessionData.getLoggedInUsers().size());
  }


  private void appendItem(Element element) {
    chatBox.getElement().appendChild(element);
    chatBox.getElement().setScrollTop(element.getOffsetTop());
  }
}

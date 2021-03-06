package org.cdidemo2.client.local;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.cdidemo2.client.shared.ActionMessage;
import org.cdidemo2.client.shared.Authenticated;
import org.cdidemo2.client.shared.ChatMessage;
import org.cdidemo2.client.shared.ClientLogin;
import org.cdidemo2.client.shared.Connect;
import org.cdidemo2.client.shared.ModeChange;
import org.cdidemo2.client.shared.NotAuthenticated;
import org.cdidemo2.client.shared.Ping;
import org.cdidemo2.client.shared.Server;
import org.cdidemo2.client.shared.ServerNotice;
import org.cdidemo2.client.shared.UserJoin;
import org.cdidemo2.client.shared.Userlist;
import org.cdidemo2.client.shared.Userpart;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@EntryPoint
public class ChatClient {
  @Inject Event<Connect> clientConnectEvent;

  @Inject Event<ClientLogin> clientLoginEvent;
  @Inject Event<Ping> pingEvent;

  @Inject ChatBox chatBox;
  @Inject ChatLogin chatLogin;
  @Inject RootPanel rootPanel;
  @Inject SessionData sessionData;

  @AfterInitialization
  private void onLoad() {
    Notifications.initFocusTracking();

    chatLogin.addOnClickCallback(new Runnable() {
      @Override
      public void run() {
        clientLoginEvent.fire(new ClientLogin(chatLogin.getNick()));
      }
    });
    chatLogin.setVisible(false);
    rootPanel.add(chatLogin);

    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        chatBox.calculateHeight();
      }
    });
    chatBox.calculateHeight();

    // begin connect attempt.
    clientConnectEvent.fire(new Connect());
  }

  private void observesNotAuthenticated(@Observes final NotAuthenticated notAuthenticated) {
    rootPanel.remove(chatBox);
    chatLogin.displayWithMessage(notAuthenticated.getMessage());
  }

  private void observesAuthenticated(@Observes final Authenticated authenticated) {
    chatLogin.setVisible(false);
    rootPanel.add(chatBox);
    sessionData.setNickName(authenticated.getNick());
    startPinging();
  }

  private void observesServerMessage(@Observes @Server ChatMessage message) {
    chatBox.addMessage(message.getNick(), message.getMessage());
  }

  public void observesActionMessage(@Observes @Server ActionMessage message) {
    chatBox.addActionMessage(message.getNick(), message.getMessage());
  }

  private void observesUserJoin(@Observes UserJoin userJoin) {
    sessionData.addLoggedInUser(userJoin.getNick());
    chatBox.addLoggedInUser(userJoin.getNick());
    chatBox.addEvent(userJoin.getNick(), " has joined the chat.");
  }

  private void observesUserlist(@Observes Userlist userlist) {
    sessionData.setLoggedInUsers(userlist.getUsers());
    updateUserList();
  }

  private void observesUserpart(@Observes Userpart userpart) {
    sessionData.removeLoggedInUser(userpart.getNick());
    updateUserList();
    chatBox.addEvent(userpart.getNick(), " has left the chat.");
  }

  private void observesNotice(@Observes ServerNotice notice) {
    chatBox.addEvent("SERVER", notice.getMessage());
  }

  private void observesModeCange(@Observes ModeChange modeChange) {
    switch (modeChange.getAction()) {
      case Kick:
        if (sessionData.getNickName().equals(modeChange.getParameter())) {
          rootPanel.remove(chatBox);
          Window.alert("You have been kicked by a channel operator.");
          break;
        }
        chatBox.addEvent("MODE", modeChange.getParameter() + " has been kicked from the chatroom.");
        sessionData.removeLoggedInUser(modeChange.getParameter());
        updateUserList();
        break;
      case Op:
        chatBox.addEvent("MODE", modeChange.getParameter() + " has been promoted to operator.");
        break;
      case Deop:
        chatBox.addEvent("MODE", modeChange.getParameter() + " has been demoted from operator.");
        break;
      case Topic:
        chatBox.addEvent("MODE", "Topic is now: " + modeChange.getParameter());
        Window.setTitle(modeChange.getParameter());
        break;
    }
  }

  private void updateUserList() {
    chatBox.clearLoggedInUsers();
    for (String nick : sessionData.getLoggedInUsers()) {
      chatBox.addLoggedInUser(nick);
    }
  }

  private void startPinging() {
    ping();
    new Timer() {
      @Override
      public void run() {
        ping();
      }
    }.scheduleRepeating(10000);
  }

  private void ping() {
    pingEvent.fire(new Ping(sessionData.getNickName()));
  }
}

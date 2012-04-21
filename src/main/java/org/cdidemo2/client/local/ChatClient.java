package org.cdidemo2.client.local;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.cdidemo2.client.shared.Authenticated;
import org.cdidemo2.client.shared.ChatMessage;
import org.cdidemo2.client.shared.ClientLogin;
import org.cdidemo2.client.shared.Connect;
import org.cdidemo2.client.shared.NotAuthenticated;
import org.cdidemo2.client.shared.Ping;
import org.cdidemo2.client.shared.Server;
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
    chatLogin.addOnClickCallback(new Runnable() {
      @Override
      public void run() {
        clientLoginEvent.fire(new ClientLogin(chatLogin.getNick()));
      }
    });
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

  private void updateUserList() {
    chatBox.clearLoggedInUsers();
    for (String nick : sessionData.getLoggedInUsers()) {
      chatBox.addLoggedInUser(nick);
    }
  }

  private void startPinging() {
    new Timer() {
      @Override
      public void run() {
        pingEvent.fire(new Ping(sessionData.getNickName()));
      }
    }.scheduleRepeating(45000);
  }
}

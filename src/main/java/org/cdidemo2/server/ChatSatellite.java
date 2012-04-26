package org.cdidemo2.server;

import org.cdidemo2.client.shared.Action;
import org.cdidemo2.client.shared.ActionMessage;
import org.cdidemo2.client.shared.AdminAction;
import org.cdidemo2.client.shared.AdminAuth;
import org.cdidemo2.client.shared.Authenticated;
import org.cdidemo2.client.shared.ChatMessage;
import org.cdidemo2.client.shared.Client;
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
import org.jboss.errai.cdi.server.events.EventConversationContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class ChatSatellite {
  @Inject @Server Event<ChatMessage> serverMessageEvent;
  @Inject @Server Event<ActionMessage> actionMessageEvent;

  @Inject Event<ServerNotice> serverNoticeEvent;
  @Inject Event<ModeChange> modeChangeEvent;
  @Inject Event<UserJoin> userJoinEvent;
  @Inject Event<Userlist> userListEvent;
  @Inject Event<Userpart> userpartEvent;

  @Inject Event<Authenticated> authenticatedEvent;
  @Inject Event<NotAuthenticated> notAuthenticatedEvent;

  private final Map<String, User> loggedInUsers = new ConcurrentHashMap<String, User>();
  private final Map<String, User> activeSessions = new ConcurrentHashMap<String, User>();

  private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

  private String adminPassword = "default123";
  private String channelTopic = "Welcome to Errai Chat!";

  @PostConstruct
  private void init() {
    ResourceBundle bundle = ResourceBundle.getBundle("chat");
    if (bundle != null) {
      adminPassword = bundle.getString("admin.password");
    }

    service.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        Iterator<User> entryIterator = loggedInUsers.values().iterator();
        while (entryIterator.hasNext()) {
          User user = entryIterator.next();
          if (user.isTimedout()) {
            entryIterator.remove();
            activeSessions.remove(user.getSessionId());
            userpartEvent.fire(new Userpart(user.getNick()));
          }
        }

      }
    }, 0, 10, TimeUnit.SECONDS);
  }

  @PreDestroy
  private void deinit() {
    service.shutdownNow();
  }

  private void observesConnect(@Observes Connect connect) {
    if (checkLoggedIn()) {
      setupClient();
    }
    else {
      notAuthenticatedEvent.fire(new NotAuthenticated("Please login to start chatting!"));
    }
  }

  private void observesClientLogin(@Observes ClientLogin login) {
    if (!checkLoggedIn()) {
      if (loggedInUsers.containsKey(login.getNick())) {
        notAuthenticatedEvent.fire(new NotAuthenticated("The nick '" + login.getNick() + "' is already in use. " +
                "Please enter a different nick."));
        return;
      }

      final String sessionId = EventConversationContext.get().getSession().getParentSessionId();
      final User user = new User(login.getNick(), sessionId);

      loggedInUsers.put(login.getNick(), user);
      activeSessions.put(sessionId, user);
    }

    userJoinEvent.fire(new UserJoin(login.getNick()));
    setupClient();
  }

  private void setupClient() {
    authenticatedEvent.fire(new Authenticated(getUser().getNick()));
    userListEvent.fire(new Userlist(loggedInUsers.keySet()));
    modeChangeEvent.fire(new ModeChange(Action.Topic, channelTopic));
  }

  private User getUser() {
    final String sessionId = EventConversationContext.get().getSession().getParentSessionId();
    return activeSessions.get(sessionId);
  }

  private boolean checkLoggedIn() {
    final String sessionId = EventConversationContext.get().getSession().getParentSessionId();
    return activeSessions.containsKey(sessionId);
  }

  private void observesAdminAuth(@Observes AdminAuth auth) {
    User user = getUser();
    if (user != null) {
      if (adminPassword.equals(auth.getPassword())) {
        user.setOperator(true);
        modeChangeEvent.fire(new ModeChange(Action.Op, user.getNick()));
      }
      else {
        serverNoticeEvent.fire(new ServerNotice("You have entered an incorrect password."));
      }
    }
  }

  private void observesAdminAction(@Observes AdminAction adminAction) {
    User user = getUser();
    if (user != null && user.isOperator()) {
      switch (adminAction.getActionType()) {
        case Op: {
          User toOp = loggedInUsers.get(adminAction.getParameter());
          if (toOp != null) {
            toOp.setOperator(true);
            notifyModeChangeFor(adminAction);
          }
          else {
            notifyNoSuchUser(adminAction.getParameter());
          }
        }
        break;

        case Deop: {
          User toDeop = loggedInUsers.get(adminAction.getParameter());
          if (toDeop != null) {
            toDeop.setOperator(false);
            notifyModeChangeFor(adminAction);
          }
          else {
            notifyNoSuchUser(adminAction.getParameter());
          }
        }
        break;

        case Kick: {
          User toKick = loggedInUsers.get(adminAction.getParameter());
          if (toKick != null) {
            notifyModeChangeFor(adminAction);
            activeSessions.values().remove(toKick);
            loggedInUsers.values().remove(toKick);
          }
          else {
            notifyNoSuchUser(adminAction.getParameter());
          }
        }

        break;

        case Topic: {
          channelTopic = adminAction.getParameter();
          notifyModeChangeFor(adminAction);
        }
      }
    }
    else {
      notifyNoAccess();
    }
  }

  private void notifyModeChangeFor(AdminAction adminAction) {
    modeChangeEvent.fire(new ModeChange(adminAction.getActionType(), adminAction.getParameter()));
  }

  private void notifyNoAccess() {
    serverNoticeEvent.fire(new ServerNotice("You must be an operator to perform this action."));
  }

  private void notifyNoSuchUser(String nick) {
    serverNoticeEvent.fire(new ServerNotice("The user '" + nick + "' is not logged in."));
  }

  private void observesActionMessage(@Observes @Client ActionMessage actionMessage) {
    if (!checkLoggedIn()) {
      serverNoticeEvent.fire(new ServerNotice("You cannot send messages because you are not logged in."));
      notAuthenticatedEvent.fire(new NotAuthenticated("You are not authenticated. Please login to chat:"));
    }
    else {
      actionMessageEvent.fire(new ActionMessage(new Date(), actionMessage.getNick(), actionMessage.getMessage()));
    }
  }

  private void observesChatMessage(@Observes @Client ChatMessage message) {
    if (!checkLoggedIn()) {
      serverNoticeEvent.fire(new ServerNotice("You cannot send messages because you are not logged in."));
      notAuthenticatedEvent.fire(new NotAuthenticated("You are not authenticated. Please login to chat:"));
    }
    else {
      serverMessageEvent.fire(new ChatMessage(new Date(), message.getNick(), message.getMessage()));
    }
  }

  private void observesPing(@Observes Ping ping) {
    final User user = getUser();
    if (user != null) {
      user.activity();
    }
  }
}

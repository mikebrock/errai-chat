package org.cdidemo2.server;

import org.cdidemo2.client.shared.Authenticated;
import org.cdidemo2.client.shared.ChatMessage;
import org.cdidemo2.client.shared.Client;
import org.cdidemo2.client.shared.ClientLogin;
import org.cdidemo2.client.shared.Connect;
import org.cdidemo2.client.shared.NotAuthenticated;
import org.cdidemo2.client.shared.Ping;
import org.cdidemo2.client.shared.Server;
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

  @Inject Event<UserJoin> userJoinEvent;
  @Inject Event<Userlist> userListEvent;
  @Inject Event<Userpart> userpartEvent;

  @Inject Event<Authenticated> authenticatedEvent;
  @Inject Event<NotAuthenticated> notAuthenticatedEvent;

  private final Map<String, User> loggedInUsers = new ConcurrentHashMap<String, User>();
  private final Map<String, User> activeSessions = new ConcurrentHashMap<String, User>();

  private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

  @PostConstruct
  private void init() {
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
    }, 0, 45, TimeUnit.SECONDS);
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
  }

  private User getUser() {
    final String sessionId = EventConversationContext.get().getSession().getParentSessionId();
    return activeSessions.get(sessionId);
  }

  private boolean checkLoggedIn() {
    final String sessionId = EventConversationContext.get().getSession().getParentSessionId();
    return activeSessions.containsKey(sessionId);

  }

  private void observesChatMessage(@Observes @Client ChatMessage message) {
    serverMessageEvent.fire(new ChatMessage(new Date(), message.getNick(), message.getMessage()));
  }

  private void observesPing(@Observes Ping ping) {
    if (ping.getNick() == null) return;

    final User user = loggedInUsers.get(ping.getNick());
    if (user != null) {
      user.activity();
    }
  }
}

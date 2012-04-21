package org.cdidemo2.server;

/**
 * @author Mike Brock
 */
public class User {
  private final static long TIMEOUT = 1000 * 60;
  private final String nick;
  private final String sessionId;
  private long lastActivity = System.currentTimeMillis();

  public User(final String nick, final String sessionId) {
    this.nick = nick;
    this.sessionId = sessionId;
  }

  public String getNick() {
    return nick;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void activity() {
    lastActivity = System.currentTimeMillis();
  }

  public boolean isTimedout() {
    return System.currentTimeMillis() - lastActivity > TIMEOUT;
  }
}

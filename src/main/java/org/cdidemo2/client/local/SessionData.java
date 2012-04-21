package org.cdidemo2.client.local;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class SessionData {
  private String nickName;
  private final Set<String> loggedInUsers = new HashSet<String>();

  public String getNickName() {
    return nickName;
  }

  public void setNickName(String nickName) {
    this.nickName = nickName;
  }

  public void setLoggedInUsers(Set<String> nick) {
    loggedInUsers.clear();
    loggedInUsers.addAll(nick);
  }

  public void addLoggedInUser(String nickName) {
    loggedInUsers.add(nickName);
  }

  public void removeLoggedInUser(String nickName) {
    loggedInUsers.remove(nickName);
  }

  public Set<String> getLoggedInUsers() {
    return Collections.unmodifiableSet(loggedInUsers);
  }
}

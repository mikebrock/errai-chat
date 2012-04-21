package org.cdidemo2.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.enterprise.client.cdi.api.Conversational;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

import java.util.Collections;
import java.util.Set;

/**
 * @author Mike Brock
 */
@Portable @Conversational
public class Userlist {
  private final Set<String> users;

  public Userlist(@MapsTo("users") Set<String> users) {
    this.users = Collections.unmodifiableSet(users);
  }

  public Set<String> getUsers() {
    return users;
  }
}

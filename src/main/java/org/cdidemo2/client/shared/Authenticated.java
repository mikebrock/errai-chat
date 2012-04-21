package org.cdidemo2.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.enterprise.client.cdi.api.Conversational;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author Mike Brock
 */
@Portable @Conversational
public class Authenticated {
  private final String nick;

  public Authenticated(@MapsTo("nick") final String nick) {
    this.nick = nick;
  }

  public String getNick() {
    return nick;
  }
}

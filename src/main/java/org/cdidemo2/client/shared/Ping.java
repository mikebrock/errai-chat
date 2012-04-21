package org.cdidemo2.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author Mike Brock
 */
@Portable
public class Ping {
  private final String nick;

  public Ping(@MapsTo("nick") String nick) {
    this.nick = nick;
  }

  public String getNick() {
    return nick;
  }
}

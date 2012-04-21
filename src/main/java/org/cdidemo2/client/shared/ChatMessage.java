package org.cdidemo2.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

import java.util.Date;

/**
 * @author Mike Brock
 */
@Portable
public class ChatMessage {
  private final Date date;
  private final String nick;
  private final String message;

  public ChatMessage(@MapsTo("date") Date date, @MapsTo("nick") String nick, @MapsTo("message") String message) {
    this.date = date;
    this.nick = nick;
    this.message = message;
  }

  public Date getDate() {
    return date;
  }

  public String getNick() {
    return nick;
  }

  public String getMessage() {
    return message;
  }
}

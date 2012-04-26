package org.cdidemo2.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.enterprise.client.cdi.api.Conversational;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author Mike Brock
 */
@Portable @Conversational
public class ServerNotice {
  private final String message;

  public ServerNotice(@MapsTo("message") String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}


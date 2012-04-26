package org.cdidemo2.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author Mike Brock
 */
@Portable
public class AdminAuth {
  private final String password;

  public AdminAuth(@MapsTo("password") String password) {
    this.password = password;
  }

  public String getPassword() {
    return password;
  }
}

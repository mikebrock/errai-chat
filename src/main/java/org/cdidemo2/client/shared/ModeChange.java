package org.cdidemo2.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author Mike Brock
 */
@Portable
public class ModeChange {
  private final Action action;
  private final String parameter;

  public ModeChange(@MapsTo("action") Action action, @MapsTo("parameter") String parameter) {
    this.action = action;
    this.parameter = parameter;
  }

  public Action getAction() {
    return action;
  }

  public String getParameter() {
    return parameter;
  }
}

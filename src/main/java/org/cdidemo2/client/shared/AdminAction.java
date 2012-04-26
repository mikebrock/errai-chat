package org.cdidemo2.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author Mike Brock
 */
@Portable
public class AdminAction {
  private final Action actionType;
  private final String parameter;

  public AdminAction(@MapsTo("actionType") Action actionType, @MapsTo("onNick") String parameter) {
    this.parameter = parameter;
    this.actionType = actionType;
  }

  public Action getActionType() {
    return actionType;
  }

  public String getParameter() {
    return parameter;
  }
}

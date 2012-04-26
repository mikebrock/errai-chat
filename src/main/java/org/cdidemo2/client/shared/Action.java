package org.cdidemo2.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock
 */
@Portable
public enum Action {
  Kick, Ban, Op, Deop, Topic
}

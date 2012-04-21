package org.cdidemo2.client.shared;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Mike Brock
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Server {
}

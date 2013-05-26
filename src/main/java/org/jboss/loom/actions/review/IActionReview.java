package org.jboss.loom.actions.review;

import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ex.MigrationException;

/**
 *  For final actions review, after they are created, but before performed.
 *  The method may change the action - e.g. addWarning(),
 *  or throw an exception.
 * 
 *  @Jira: MIGR-101
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IActionReview {

    void review( IMigrationAction action ) throws MigrationException;

    public void setContext( MigrationContext ctx );
    public void setConfig( Configuration config );
}

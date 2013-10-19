package org.jboss.loom.actions.review;

import org.jboss.loom.conf.Configuration;
import org.jboss.loom.ctx.MigrationContext;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public abstract class ActionReviewBase implements IActionReview {
    
    private MigrationContext ctx;
    private Configuration config;

    public MigrationContext getContext() { return ctx; }
    @Override public void setContext( MigrationContext ctx ) { this.ctx = ctx; }
    public Configuration getConfig() { return config; }
    @Override public void setConfig( Configuration config ) { this.config = config; }

}

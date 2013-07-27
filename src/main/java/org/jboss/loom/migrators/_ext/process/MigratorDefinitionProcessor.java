package org.jboss.loom.migrators._ext.process;


import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.apache.commons.lang.StringUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.actions.CopyFileAction;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.actions.ModuleCreationAction;
import org.jboss.loom.actions.XsltAction;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators._ext.ContainerOfStackableDefs;
import org.jboss.loom.migrators._ext.DefinitionBasedMigrator;
import org.jboss.loom.migrators._ext.MigratorDefinition;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.utils.XmlUtils;
import org.jboss.loom.utils.el.IExprLangEvaluator;
import org.jboss.loom.utils.el.IExprLangEvaluator.JuelCustomResolverEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Processor for migrator definitions from .mig.xml files.
 *  These define:
 * 
 *    * JAXB classes, coded as Groovy files.
 *    * Queries which result in a list of IConfigFragment (typically, backed by those JAXB classes).
 *    * ForEach iterators which iterate over results of queries.
 *      * ForEach's define a variable which is available in EL and sub-contexts.
 * 
 *    * Nested elements which result in actions.
 *      * These actions can depend on each other.
 *      * Actions may contain warnings.
 *      * Some strings may contain EL expressions - e.g. ${varName.bean.path}
 * 
 *  TODO:
 * 
 *  This is the first attempt on implementation. It's a bit too procedural I guess and could be rewritten to something smarter,
 *  perhaps Ant tasks engine could be used as a basis.
 * 
 *  Other thing to improve could be to process the instructions in the order of appearance in the XML file.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MigratorDefinitionProcessor implements IExprLangEvaluator.IVariablesProvider {
    private static final Logger log = LoggerFactory.getLogger( MigratorDefinitionProcessor.class );
    
    final Stack<ProcessingStackItem> stack = new Stack();
    
    final DefinitionBasedMigrator dbm;
    
    private JuelCustomResolverEvaluator eval = new JuelCustomResolverEvaluator( this );


    public MigratorDefinitionProcessor( DefinitionBasedMigrator dbm ) {
        this.stack.push( (ProcessingStackItem) new RootContext().setVariable("mig", dbm).setVariable("conf", dbm.getConfig()));
        this.dbm = dbm;
    }
    
    
    /**
     *  Look up the top-most variable on the stack. Return null if not found.
     *  I decided to prefer this method over recursive calls down the stack, using parentContext reference,
     *  because this way I have more control. Could become handy later.
     */
    public Object getVariable( String name ){
        for( ProcessingStackItem context : stack ) {
            Object var = context.getVariable( name );
            if( var != null )
                return var;
        }
        return null;
    }

    
    public List<IMigrationAction> process( MigratorDefinition cont ) throws MigrationException {
        return this.processChildren( cont );
    }

    /**
     *  Recursively processes the .mig.xml descriptor into Actions.
     *  TODO: Could be better to put the stack manipulation to the beginning and end of the method.
     */
    private List<IMigrationAction> processChildren( ContainerOfStackableDefs cont ) throws MigrationException {
        
        List<IMigrationAction> actions = new LinkedList();
        
        // ForEach defs.
        if( cont.hasForEachDefs() )
        for( MigratorDefinition.ForEachDef forEachDef : cont.getForEachDefs() ) {
            
            DefinitionBasedMigrator.ConfigLoadResult queryResult = this.dbm.getQueryResultByName( forEachDef.queryName ); 
            if( null == queryResult )
                throw new MigrationException("Query '"+forEachDef.queryName+"' not found. Needed at " + XmlUtils.formatLocation(forEachDef.location));
            
            ForEachContext forEachContext = new ForEachContext( forEachDef, this );
            this.stack.push( forEachContext );
            
            // For each item in query result...
            //for( IConfigFragment configFragment : queryResult.configFragments ) {
            for( IConfigFragment configFragment : forEachContext ) {
                // A variable is set automatically in next().
                
                // Recurse.
                this.processChildren( forEachDef );
            }
            this.stack.pop();
        }
        
        // Action definitions.
        // TODO: Currently, actions processing is hard-coded. This needs to be brought to meta-data.
        if( cont.hasActionDefs() )
        for( MigratorDefinition.ActionDef actionDef : cont.getActionDefs() ) {
            IMigrationAction action;
            switch( actionDef.typeVal ){
                case "manual":
                    action = new ManualAction();
                    // warning
                    // forEach
                    break;
                case "copy": {
                    String src  = actionDef.attribs.get("src");
                    String dest = actionDef.attribs.get("dest");
                    String ifExistsS = actionDef.attribs.get("ifExists");
                    CopyFileAction.IfExists ifExists = CopyFileAction.IfExists.valueOf( ifExistsS );
                    action = new CopyFileAction( DefinitionBasedMigrator.class, new File(src), new File(dest), ifExists ); 
                } break;
                case "xslt": {
                    String srcS      = actionDef.attribs.get("src");
                    String destS     = actionDef.attribs.get("dest");
                    String xsltS     = actionDef.attribs.get("xlst");
                    
                    File src      = new File( srcS );
                    File dest     = new File( destS );
                    File xslt     = new File( xsltS );
                    
                    String ifExistsS = actionDef.attribs.get("ifExists");
                    CopyFileAction.IfExists ifExists = CopyFileAction.IfExists.valueOf( ifExistsS );
                    boolean failIfExists = "true".equals( actionDef.attribs.get("failIfExists") );
                    action = new XsltAction( DefinitionBasedMigrator.class, src, xslt, dest, ifExists, failIfExists ); 
                }   break;
                case "cli": {
                    String cliScript = actionDef.attribs.get("cliScript");
                    ModelNode modelNode = ModelNode.fromString( cliScript );
                    action = new CliCommandAction( DefinitionBasedMigrator.class, cliScript, modelNode ); 
                } break;
                case "module": {
                    String name = actionDef.attribs.get("name");
                    String jarS = actionDef.attribs.get("jar");
                    File jar     = new File( jarS );
                    String[] deps = parseDeps( actionDef.attribs.get("deps") );
                    Configuration.IfExists ifExists = Configuration.IfExists.valueOf("ifExists");
                    action = new ModuleCreationAction( DefinitionBasedMigrator.class, name, deps, jar, ifExists );
                } break;
                default: 
                    throw new MigrationException("Unsupported action type '" + actionDef.typeVal + "' in " + cont.location.getSystemId());
            }
            
            // Recurse
            this.stack.push( new ActionContext( action ) );
            this.processChildren( actionDef );
            this.stack.pop();
            
            actions.add( action );
        }
        return actions;
    }// process();


    /**
     *  Parses syntax "foo ?bar baz" into String[]{"foo", null, "bar", "baz"}.
     *  (? and null means that the following dep is optional.)
     */
    private String[] parseDeps( String str ) {
        List<String> deps = new LinkedList();
        for( String name : StringUtils.split(str) ){
            if( name.charAt(0) == '?' ){
                deps.add( null );
                name = name.substring(1);
            }
            deps.add( name );
        }
        return deps.toArray( new String[deps.size()] );
    }
    
}// class

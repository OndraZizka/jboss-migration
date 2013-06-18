package org.jboss.loom.utils.el;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.MapELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *  Don't look, this is a really ugly code.
 *  An interface and it's various implementations.
 * 
 *  Evaluates expression like:
 * 
    String greet = new IExprLangEvaluator.SimpleEvaluator().evaluateEL(
        "Hello ${person.name} ${person.surname}, ${person.age}!", 
        new HashMap(){{
            put("person", new Person("Ondra"));
        }}
    );
 *  
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IExprLangEvaluator {
    
    public String evaluateEL( String template ); // , Map<String, ? extends Object> properties
    
            
    public static class SimpleEvaluator implements IExprLangEvaluator {
        private static final org.slf4j.Logger log = LoggerFactory.getLogger( SimpleEvaluator.class );
        
        private final Map<String, ? extends Object> properties;

        public SimpleEvaluator( Map<String, ? extends Object> properties ) {
            this.properties = properties;
        }
        
        
        @Override
        public String evaluateEL( String template ) {
            
            StringTokenizer st = new StringTokenizer( template );
            String text = st.nextToken("${");
            StringBuilder sb = new StringBuilder();
            
            // Parse the template: "Hello ${person.name} ${person.surname}, ${person.age}!"
            do{
                try {
                    sb.append(text);
                    if( ! st.hasMoreTokens() )
                        break;

                    // "${foo.bar[a]"
                    String expr  = st.nextToken("}");
                    // "foo.bar[a].baz"
                    expr = expr.substring(2);
                    // "foo"
                    String var = StringUtils.substringBefore( expr, ".");

                    Object subject = properties.get( var );
                    
                    // "bar[a].baz"
                    String propPath = StringUtils.substringAfter( expr, ".");
                    
                    sb.append( resolveProperty2( subject, propPath ) );

                    text = st.nextToken("${");
                    text = text.substring(1);
                } catch( NoSuchElementException ex ){
                    // Unclosed ${
                    log.warn("Unclosed ${ expression, missing } : " + template);
                }
            } while( true );

            return sb.toString();
        }


        // Simple
        private String resolveProperty( Object subject, String propPath ) {
            if( subject == null ) return "";
            return subject.toString();
        }
        
        // BeanUtils
        private String resolveProperty2( Object subject, String propPath ) {
            if( subject == null ) return "";
            
            if( propPath == null || propPath.isEmpty() ) return subject.toString();
            
            try {
                return "" + PropertyUtils.getProperty( subject, propPath );
            } catch( IllegalAccessException | InvocationTargetException | NoSuchMethodException ex ) {
                log.warn("Failed resolving '" + propPath + "' on " + subject + ":\n    " + ex.getMessage());
                if( log.isTraceEnabled() )
                    log.trace("    Stacktrace:\n", ex);
                return "";
            }
        }
        
    }// class SimpleEvaluator
    
    
    static final ExpressionFactory JUEL_FACTORY = new de.odysseus.el.ExpressionFactoryImpl();
    
    /**
     * JUEL: http://juel.sourceforge.net/guide/start.html
     */ 
    public static class JuelSimpleEvaluator implements IExprLangEvaluator {
        
        private final Map<String, ? extends Object> properties;
        
        public JuelSimpleEvaluator( Map<String, ? extends Object> properties ) {
            this.properties = properties;
        }
        
        
        public String evaluateEL( String expr ) {
    
            // Pre-fill a context with values.
            de.odysseus.el.util.SimpleContext context = new de.odysseus.el.util.SimpleContext();
            for( Map.Entry<String, ? extends Object> entry : properties.entrySet() ) {
                context.setVariable( entry.getKey(), JUEL_FACTORY.createValueExpression(entry.getValue(), String.class ));
            }
            
            // Create the value expression and evaluate.
            ValueExpression valueExpr = JUEL_FACTORY.createValueExpression(context, expr, String.class);
            return (String) valueExpr.getValue(context);
        }
    }
    
    /**
     * JUEL: http://juel.sourceforge.net/guide/start.html
     */ 
    public static class JuelCustomResolverEvaluator implements IExprLangEvaluator {
        
        private final IVariablesProvider varProvider;

        public JuelCustomResolverEvaluator( IVariablesProvider variableProvider ) {
            this.varProvider = variableProvider;
        }
        
        
        public String evaluateEL( String expr ) {
    
            //ELResolver resolver;
            final CompositeELResolver resolver = new CompositeELResolver();
            resolver.add(new MapELResolver() );
            resolver.add(new BeanELDefaultStringResolver("") );
            
            
            //de.odysseus.el.util.SimpleContext context = new de.odysseus.el.util.SimpleContext();
            ELContext context = new ELContext() {
                
                @Override public ELResolver getELResolver() { return resolver; }

                @Override public FunctionMapper getFunctionMapper() { return THROW_MAPPER; }

                @Override public VariableMapper getVariableMapper() {
                    /*return new VariableMapper() {
                        @Override public ValueExpression resolveVariable( String variable ) {
                            return JUEL_FACTORY.createValueExpression( properties.get( variable ), Object.class );
                        }
                        @Override public ValueExpression setVariable( String variable, ValueExpression expression ) {
                            throw new UnsupportedOperationException( "Read-only, can't set: " + variable );
                        }
                    };*/
                    return new ProvidedVariableMapper( varProvider );
                    
                }
            };
            
            ValueExpression valueExpr = JUEL_FACTORY.createValueExpression(context, expr, String.class);
            try {
                return (String) valueExpr.getValue(context);
            }
            catch(javax.el.PropertyNotFoundException ex){
                throw new IllegalArgumentException("Can't eval '" + expr + "':\n    " + ex.getMessage(), ex);
            }
        }
    }
    /**/
    
    
    
    public static interface IVariablesProvider<T> {
        T getVariable( String name );
    }
    
    public class ProvidedVariableMapper extends VariableMapper {
        private final IVariablesProvider<Object> provider;
        public ProvidedVariableMapper( IVariablesProvider<Object> provider ) {
            this.provider = provider;
        }
        
        @Override public ValueExpression resolveVariable( String variable ) {
            return JUEL_FACTORY.createValueExpression( provider.getVariable( variable ), Object.class );
        }
        @Override public ValueExpression setVariable( String variable, ValueExpression expression ) {
            throw new UnsupportedOperationException( "Read-only, can't set: " + variable );
        }
    };
    

    
    static final FunctionMapper THROW_MAPPER = 
            new FunctionMapper() {
                @Override public Method resolveFunction( String prefix, String localName ) {
                    throw new UnsupportedOperationException( "No functions supported." );
                }
            };

    /**
     *  Puts a custom default string in place of unresolved property, instead of throwing an ex.
     */
    public static class BeanELDefaultStringResolver extends BeanELOpenResolver {

        private final String defaultString;
        public BeanELDefaultStringResolver( String defaultString ) {
            this.defaultString = defaultString;
        }
        @Override protected Object onPropertyNotFoundRead( Object base, Object property ) {
            return this.defaultString;
        }
    }
        
}// class

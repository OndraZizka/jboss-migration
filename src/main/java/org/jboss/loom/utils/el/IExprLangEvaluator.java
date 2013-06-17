package org.jboss.loom.utils.el;


import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleResolver;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.el.BeanELResolver;
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
    
    public String evaluateEL( String template, Map<String, Object> properties );
    
    
    public static class SimpleEvaluator implements IExprLangEvaluator {
        private static final org.slf4j.Logger log = LoggerFactory.getLogger( SimpleEvaluator.class );
        
        @Override
        public String evaluateEL( String template, Map<String, Object> properties ) {
            
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
            } catch(     IllegalAccessException | InvocationTargetException | NoSuchMethodException ex ) {
                log.warn("Failed resolving '" + propPath + "' on " + subject + ":\n    " + ex.getMessage(), ex);
                return "";
            }
        }
        
    }// class SimpleEvaluator
    
    
    /**
     * JUEL: http://juel.sourceforge.net/guide/start.html
     */ 
    public static class JuelSimpleEvaluator implements IExprLangEvaluator {
        public String evaluateEL( String expr, Map<String, Object> properties ) {
    
            ExpressionFactory factory = new de.odysseus.el.ExpressionFactoryImpl();
            
            de.odysseus.el.util.SimpleContext context = new de.odysseus.el.util.SimpleContext();
            for( Map.Entry<String, Object> entry : properties.entrySet() ) {
                context.setVariable( entry.getKey(), factory.createValueExpression(entry.getValue(), String.class ));
            }
            ValueExpression valueExpr = factory.createValueExpression(context, expr, String.class);
            
            return (String) valueExpr.getValue(context);
        }
    }
    /**/
    /**
     * JUEL: http://juel.sourceforge.net/guide/start.html
     */ 
    public static class JuelCustomResolverEvaluator implements IExprLangEvaluator {
        public String evaluateEL( String expr, final Map<String, Object> properties ) {
    
            final ExpressionFactory factory = new de.odysseus.el.ExpressionFactoryImpl();
            
            //ELResolver resolver;
            final CompositeELResolver resolver = new CompositeELResolver();
            resolver.add(new MapELResolver() );
            resolver.add(new BeanELResolver() );
            
            
            //de.odysseus.el.util.SimpleContext context = new de.odysseus.el.util.SimpleContext();
            ELContext context = new ELContext() {
                @Override public ELResolver getELResolver() {
                    return resolver;
                }

                @Override public FunctionMapper getFunctionMapper() {
                    throw new UnsupportedOperationException( "Not functions supported." );
                }

                @Override
                public VariableMapper getVariableMapper() {
                    return new VariableMapper() {
                        @Override public ValueExpression resolveVariable( String variable ) {
                            return factory.createValueExpression( properties.get( variable ), String.class );
                        }
                        @Override public ValueExpression setVariable( String variable, ValueExpression expression ) {
                            throw new UnsupportedOperationException( "Read-only, can't set: " + variable );
                        }
                    };
                }
            };
            
            ValueExpression valueExpr = factory.createValueExpression(context, expr, String.class);
            return (String) valueExpr.getValue(context);
        }
    }
    /**/

        
}// class

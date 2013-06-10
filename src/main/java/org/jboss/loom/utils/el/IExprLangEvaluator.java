package org.jboss.loom.utils.el;


import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IExprLangEvaluator {
    
    public String evaluateEL( String expr, Map<String, String> properties );
    
    
    public static class SimpleEvaluator implements IExprLangEvaluator {
        @Override
        public String evaluateEL( String expr, Map<String, String> properties ) {
            StringTokenizer st = new StringTokenizer( expr );
            String text = st.nextToken("${");
            StringBuilder sb = new StringBuilder();
            do{
                try {
                    sb.append(text);
                    if( ! st.hasMoreTokens() )
                        break;

                    String var  = st.nextToken("}");
                    var = var.substring(2);
                    Object resolved = properties.get( var );
                    sb.append( resolved == null ? "" : resolved.toString() );

                    text = st.nextToken("${");
                    text = text.substring(1);
                } catch( NoSuchElementException ex ){
                    // Unclosed ${
                }
            } while( true );

            return sb.toString();
        }
    }// Simple
    
    
    /**
     * @deprecated  TODO:  Use some EL library. http://juel.sourceforge.net/guide/start.html
     *  
     * 
    public static class JuelEvaluator implements IExprLangEvaluator {
        public static String evaluateEL( String expr, Map<String, String> properties ) {
    
            ExpressionFactory factory = new ExpressionFactoryImpl();

            de.odysseus.el.util.SimpleContext context = new de.odysseus.el.util.SimpleContext();
            for( Map.Entry<String, String> entry : properties.entrySet() ) {
                context.setVariable( entry.getKey(), factory.createValueExpression(entry.getValue(), String.class ));
            }

            ValueExpression valueExpr = factory.createValueExpression(context, expr, String.class);

            return (String) valueExpr.getValue(context);
        }
    }
     */

    
    
    /*
    public static class JuelEvaluator implements IExprLangEvaluator {
        public static String evaluateEL( String expr, Map<String, String> properties ) {
    
            ExpressionFactory factory = new ExpressionFactoryImpl();

            //create a map with some variables in it
            Map<Object, Object> userMap = new HashMap();
            userMap.put( "x", new Integer( 123 ) );
            userMap.put( "y", new Integer( 456 ) );

            de.odysseus.el.util.SimpleContext context = new de.odysseus.el.util.SimpleContext();
            context.setVariable("foo", factory.createValueExpression("bar", String.class));

            ValueExpression e = factory.createValueExpression(context, "Hello ${foo}!", String.class);
            System.out.println(e.getValue(context)); // --> Hello, bar!
        }
    }*/
    
}// class

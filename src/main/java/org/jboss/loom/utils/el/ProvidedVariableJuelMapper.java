package org.jboss.loom.utils.el;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

/**
 *  Delegates the variable resolving to an IVariablesProvider provided to the constructor.
 *  Read-only.
 */
public class ProvidedVariableJuelMapper extends javax.el.VariableMapper {
    private static final ExpressionFactory JUEL_FACTORY = new de.odysseus.el.ExpressionFactoryImpl();
    

    private final IExprLangEvaluator.IVariablesProvider<Object> provider;

    
    public ProvidedVariableJuelMapper( IExprLangEvaluator.IVariablesProvider<Object> provider ) {
        this.provider = provider;
    }
    

    @Override public ValueExpression resolveVariable( String variable ) {
        return JUEL_FACTORY.createValueExpression( this.provider.getVariable( variable ), Object.class );
    }
    
    @Override public ValueExpression setVariable( String variable, ValueExpression expression ) {
        throw new UnsupportedOperationException( "Read-only, can't set: " + variable );
    }
    
}// class
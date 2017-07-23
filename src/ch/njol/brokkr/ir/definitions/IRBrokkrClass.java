package ch.njol.brokkr.ir.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ir.uses.IRMemberUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRBrokkrClass extends AbstractIRBrokkrTypeDefinition implements IRClassDefinition {
	
	public IRBrokkrClass(final ASTTypeDeclaration declaration) {
		super(declaration);
	}
	
	@Override
	public @Nullable IRAttributeImplementation getAttributeImplementation(@NonNull final IRAttributeDefinition definition) {
		for (final IRMemberRedefinition m : members()) {
			if (m instanceof IRAttributeImplementation && ((IRAttributeImplementation) m).definition().equalsMember(definition))
				return (IRAttributeImplementation) m;
		}
		return null;
	}

}
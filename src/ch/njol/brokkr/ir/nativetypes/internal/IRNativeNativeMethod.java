package ch.njol.brokkr.ir.nativetypes.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRResultDefinition;
import ch.njol.brokkr.ir.definitions.IRResultRedefinition;
import ch.njol.brokkr.ir.uses.IRSimpleTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRNativeNativeMethod implements IRAttributeImplementation, IRAttributeDefinition {
	
	private final Method method;
	private final List<IRParameterRedefinition> parameters = new ArrayList<>();
	private final IRResultRedefinition result;
	private final String name;
	
	@SuppressWarnings({"null", "unchecked"})
	public IRNativeNativeMethod(final Method method, final String name) {
		this.method = method;
		this.name = name;
		final Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++)
			parameters.add(new Parameter(i, IRSimpleNativeClass.get((Class<? extends InterpretedNativeObject>) parameterTypes[i])));
		result = new Result("result", IRSimpleNativeClass.get((Class<? extends InterpretedNativeObject>) method.getReturnType()));
	}
	
	private class Parameter implements IRParameterDefinition {
		
		private final int index;
		private final IRSimpleNativeClass type;
		
		public Parameter(final int index, final IRSimpleNativeClass type) {
			this.index = index;
			this.type = type;
		}
		
		@Override
		public String name() {
			return "" + index;
		}
		
		@Override
		public IRTypeUse type() {
			return new IRSimpleTypeUse(type);
		}
		
		@Override
		public @Nullable InterpretedObject defaultValue(final InterpreterContext context) {
			return null; // Java has no default parameter values, though they could be added via annotations
		}
		
	}
	
	private class Result implements IRResultDefinition {
		
		private final String name;
		private final IRSimpleNativeClass type;
		
		public Result(final String name, final IRSimpleNativeClass type) {
			this.name = name;
			this.type = type;
		}
		
		@Override
		public String name() {
			return name;
		}
		
		@Override
		public IRTypeUse type() {
			return new IRSimpleTypeUse(type);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IRTypeUse targetType() {
		return new IRSimpleTypeUse(IRSimpleNativeClass.get((Class<? extends InterpretedNativeObject>) method.getDeclaringClass()));
	}
	
	@Override
	public List<IRParameterRedefinition> parameters() {
		return parameters;
	}
	
	@Override
	public List<IRResultRedefinition> results() {
		return Collections.singletonList(result);
	}
	
	@Override
	public List<IRError> errors() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean isModifying() {
		return true; // TODO annotation? definition in Brokkr code? or both just to make sure?
	}
	
	@Override
	public boolean isVariable() {
		return false;
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(method.getModifiers());
	}
	
	@Override
	public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
		assert !allResults;
		final Object[] args = new Object[method.getParameterCount()];
		for (final Entry<IRParameterDefinition, InterpretedObject> e : arguments.entrySet()) {
			args[Integer.parseInt(e.getKey().name())] = e.getValue(); // TODO native params have names if defined in Brokkr code (or are those redefinitions?)
		}
		try {
			final InterpretedObject o = (InterpretedObject) method.invoke(thisObject, args);
			assert o != null : method;
			return o;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return null; // TODO find declaration in Brokkr code
	}
	
	@Override
	public int hashCode() {
		return method.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final IRNativeNativeMethod other = (IRNativeNativeMethod) obj;
		return method.equals(other.method);
	}
	
	@Override
	public boolean equalsMember(final IRMemberRedefinition other) {
		if (getClass() != other.getClass())
			return false;
		return method.equals(((IRNativeNativeMethod) other).method);
	}
	
}
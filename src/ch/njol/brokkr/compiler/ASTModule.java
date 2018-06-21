package ch.njol.brokkr.compiler;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElement;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTLink;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;
import ch.njol.brokkr.ast.AbstractASTElement;
import ch.njol.brokkr.common.ModuleIdentifier;
import ch.njol.brokkr.compiler.Token.UppercaseWordToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;

/**
 * Brokkr code is divided into modules which can communicate using public interfaces.
 * 
 * @author Peter Güttinger
 */
public class ASTModule extends ASTModuleFileElement {
	
	public @Nullable ModuleIdentifier id;
	
	public final Modules modules;
	
	public ASTModule(final Modules modules, final ModuleIdentifier id) {
		this.modules = modules;
		this.id = id;
	}
	
	// for loading only
	private ASTModule(final Modules modules) {
		this.modules = modules;
		id = null;
	}
	
	@Override
	public IRContext getIRContext() {
		return modules.irContext;
	}
	
	public String version = "0.1";
	
	public String encoding = "UTF-8";
	// module files are always UTF-8 (and everything except possibly custom data is ASCII)
	
	public String language = "Brokkr-0.1"; // instantly backwards and forwards compatible! 
	
	// TODO more info?
	// TODO warn about missing values? leaving the default value might be good or bad...
	
	// TODO dependencies? or manage those differently?
	// TODO change to dependencies with: ID, version, imports, defaultImportPrefix (and Suffix?)
	public Map<ModuleIdentifier, List<Import>> imports = new HashMap<>();
	
	public final static class Import extends AbstractASTElement<Import> {
		public ASTLink<IRTypeDefinition> type = new ASTLink<IRTypeDefinition>(this) {
			@Override
			protected @Nullable IRTypeDefinition tryLink(final String name) {
				final ASTModule module = getParentOfType(ASTModule.class);
				assert module != null;
				for (final Entry<ModuleIdentifier, List<Import>> e : module.imports.entrySet()) {
					if (!e.getValue().stream().anyMatch(l -> l == Import.this)) // find module of this import // TODO improve this to directly access parent element
						continue;
					final ASTModule importedModule = module.modules.get(e.getKey());
					return importedModule == null ? null : importedModule.getDeclaredType(name);
				}
				return null;
			}
		};
		public String alias;
		
		@SuppressWarnings({"null", "unused"})
		private Import() {}
		
		public Import(final WordToken type, final @Nullable String alias) {
			this.type.setName(type);
			this.alias = alias == null ? type.word : alias;
		}
		
		@Override
		public String toString() {
			return type.getName() + (alias.equals(type.getName()) ? "" : " as " + alias);
		}
		
		@Override
		protected Import parse() throws ParseException {
			final UppercaseWordToken typeName = oneTypeIdentifierToken();
			type.setName(typeName);
			alias = typeName.word; // make sure 'alias' not null even if the next lines fail
			if (try_("as"))
				alias = oneTypeIdentifier();
			return this;
		}
	}
	
	@Override
	protected @Nullable String checkFieldValue(@NonNull final String field, @NonNull final Object value) {
		if (field.equals("encoding")) {
			if (!Charset.isSupported((String) value))
				return "Unsupported encoding (on this JVM)";
		}
		return null;
	}
	
	private final transient Map<String, ASTBrokkrFile> files = new HashMap<>();
	
	public void clearFiles() {
		for (final ASTBrokkrFile bf : files.values()) {
			bf.module = null;
			bf.invalidateSubtree();
		}
		files.clear();
	}
	
	public void clearFile(final String file) {
		final ASTBrokkrFile ast = files.remove(file);
		if (ast != null) {
			ast.module = null;
			ast.invalidateSubtree();
		}
	}
	
	public void registerFile(final String file, final ASTBrokkrFile ast) {
		files.put(file, ast);
		ast.module = this;
	}
	
	public List<IRTypeDefinition> findDeclaredTypes(final String name) {
		final List<IRTypeDefinition> r = new ArrayList<>();
		for (final ASTBrokkrFile ast : files.values()) {
			for (final ASTElement e : ast.declarations) {
				if (e instanceof ASTTypeDeclaration && name.equals(((ASTTypeDeclaration) e).name()))
					r.add(((ASTTypeDeclaration) e).getIR());
			}
		}
		return r;
	}
	
	public @Nullable IRTypeDefinition getDeclaredType(final String name) {
		for (final ASTBrokkrFile ast : files.values()) {
			for (final ASTElement e : ast.declarations) {
				if (e instanceof ASTTypeDeclaration && name.equals(((ASTTypeDeclaration) e).name()))
					return ((ASTTypeDeclaration) e).getIR();
			}
		}
		return null;
	}
	
	public @Nullable IRTypeDefinition getType(final String name) {
		final IRTypeDefinition declaredType = getDeclaredType(name);
		if (declaredType != null)
			return declaredType;
		for (final Entry<ModuleIdentifier, List<Import>> e : imports.entrySet()) {
			for (final Import i : e.getValue()) {
				if (i.alias.equals(name)) {
					final IRTypeDefinition t = i.type.get();
					if (t != null)
						registerInvalidateListener(t); // if this module is modified, any types accessed this way must be re-checked
					return t;
				}
			}
		}
		return null;
	}
	
	// TODO make this a code template instead
	private final static Map<String, String[]> defaultImports = new HashMap<>();
	static {
		defaultImports.put("lang", new String[] {"Any", "Object", "Function", "Procedure", "Type", "Tuple", "Boolean", "Kleenean", //
				"Copyable", "Comparable", "Orderable", "Relation", "Character", "String", //
				"Number", "Integer", "Int", "UInt", "Int8", "Int16", "Int32", "Int64", "UInt8", "UInt16", "UInt32", "UInt64", "Byte"});
		defaultImports.put("util", new String[] {"Vector", "Matrix"});
		defaultImports.put("util.collection", new String[] {"Collection", "Stream", "List", "Map", "Set", "Tree"});
	}
	
	/**
	 * Inserts defaults, for example a lot of standard library imports like Boolean and List.
	 * <p>
	 * These are not enforced, as e.g. replacing Number with a more general interface might be useful for a math application.
	 */
	public void addDefaults() {
		for (final Entry<String, @NonNull String[]> dis : defaultImports.entrySet()) {
			final List<Import> is = imports.getOrDefault(dis.getKey(), new ArrayList<>());
			for (final String di : dis.getValue())
				is.add(new Import(new UppercaseWordToken(di, 0, 1), null));
			imports.put(new ModuleIdentifier(dis.getKey()), is);
		}
	}
	
	public final static ASTModule load(final Modules modules, final TokenStream tokens) {
		final ASTModule m = new ASTModule(modules);
		try {
			m.parse(tokens);
		} catch (final ParseException e) {}
		m.compactFatalParseErrors();
		return m;
	}
	
	public void save(final Writer out) throws IOException {
		for (final List<Import> l : imports.values()) {
			l.sort((i1, i2) -> {
				final String n1 = i1.type.getName(), n2 = i2.type.getName();
				return n1 == null ? 0 : n1.compareTo(n2);
			});
		}
		save(this, out, null);
		out.flush();
	}
	
	@Override
	public String toString() {
		return "" + id;
	}
	
}
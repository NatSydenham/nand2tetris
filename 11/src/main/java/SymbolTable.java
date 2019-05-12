import java.util.HashMap;

public class SymbolTable {

    /**
     * A symbol table that associates names with information needed for Jack compilation:
     * type, kind, and running index. The symbol table has 2 nested scopes: class, and subroutine.
     * <p>
     * When compiling code, any identifier not found in the symbol table may be assumed to be a
     * subroutine name or a class name. Since the Jack language syntax rules suffice for distinguishing
     * between these two possibilities, and since no “linking” needs to be done by the compiler, these
     * identifiers do not have to be kept in the symbol table.
     */


    private HashMap<String, Symbol> classScope;
    private HashMap<String, Symbol> subroutineScope;
    private int numArg = 0;
    private int numVar = 0;
    private int numStatic = 0;
    private int numField = 0;

    public SymbolTable() {
        this.classScope = new HashMap<>();
        this.subroutineScope = new HashMap<>();
    }

    /**
     * Starts a new subroutine scope
     * (ie: erases all names in the previous subroutine's scope)
     */

    public void startSubroutine() {
        subroutineScope.clear();
        numVar = 0;
        numArg = 0;
    }

    /**
     * Defines a new identifier of a given name, type and kind, and assigns it a running index.
     * Static and Field identifiers have a class scope, while arg and var identifiers have a subroutine scope.
     *
     * @param name The name of the identifier.
     * @param type The type of the identifier.
     * @param kind The kind of the identifier, either STATIC, FIELD, ARG, or VAR
     */

    public void define(String name, String type, Kind kind) {
        if (kind == Kind.ARG || kind == Kind.VAR) {
            if (kind == Kind.ARG) {
                numArg += 1;
                subroutineScope.put(name, new Symbol(type, kind, numArg));
            } else {
                numVar += 1;
                subroutineScope.put(name, new Symbol(type, kind, numVar));
            }
        } else if (kind == Kind.FIELD || kind == Kind.STATIC) {
            if (kind == Kind.FIELD) {
                numField += 1;
                classScope.put(name, new Symbol(type, kind, numField));
            } else {
                numStatic += 1;
                classScope.put(name, new Symbol(type, kind, numStatic));
            }
        }
    }


    /**
     * Returns the number of variables of a given kind defined within the given scope.
     *
     * @param kind the kind of variable
     */

    public int varCount(Kind kind) throws Exception {
        switch (kind) {
            case ARG:
                return numArg;
            case VAR:
                return numVar;
            case FIELD:
                return numField;
            case STATIC:
                return numStatic;
            default:
                throw new Exception("Not valid kind.");
        }
    }


    /**
     * Returns the kind of the named identifier in the current scope.
     * Returns NONE if the identifier is unknown in the current scope.
     *
     * @param name The name of the identifier.
     * @return the kind of the identifier with name name.
     */

    public Kind kindOf(String name) {
        if (subroutineScope.containsKey(name)) {
            return subroutineScope.get(name).getKind();
        } else if (classScope.containsKey(name)) {
            return classScope.get(name).getKind();
        } else {
            return Kind.NONE;
        }
    }

    /**
     * Returns the type of the named identifier in the current scope.
     *
     * @param name The name of the identifier.
     * @return The type of the identifier with Name name.
     */

    public String typeOf(String name) {
        if (subroutineScope.containsKey(name)) {
            return subroutineScope.get(name).getType();
        } else if (classScope.containsKey(name)) {
            return classScope.get(name).getType();
        } else {
            return "none";
        }
    }

    /**
     * Returns the index assigned to named identifier.
     *
     * @param name The name of the identifier.
     * @return The index assigned to the named identifier.
     */

    public int indexOf(String name){
        if (subroutineScope.containsKey(name)) {
            return subroutineScope.get(name).getIndex();
        } else if (classScope.containsKey(name)) {
            return classScope.get(name).getIndex();
        } else {
            return 0;
        }
    }
}

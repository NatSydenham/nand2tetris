public enum Kind {

    STATIC, FIELD, ARG, VAR, NONE;

    public static Kind stringToKind(String string){
        switch(string){
            case "static":
                return Kind.STATIC;
            case "field":
                return Kind.FIELD;
            case "arg":
                return Kind.ARG;
            case "var":
                return Kind.VAR;
            default:
                return Kind.NONE;
        }
    }

    public static String kindToSegment(Kind kind){
        switch (kind){
            case STATIC:
                return "static";
            case FIELD:
                return "this";
            case ARG:
                return "argument";
            case VAR:
                return "local";
            default:
                return "none";

        }
    }
}

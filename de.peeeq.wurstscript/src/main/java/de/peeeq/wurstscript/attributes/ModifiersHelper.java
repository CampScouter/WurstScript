package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;

public class ModifiersHelper {

    public static boolean isPublic(HasModifier e) {
        return containsType(e.getModifiers(), VisibilityPublic.class);
    }

    public static boolean isProtected(HasModifier e) {
        return containsType(e.getModifiers(), VisibilityProtected.class);
    }

    public static boolean isPublicRead(HasModifier e) {
        return containsType(e.getModifiers(), VisibilityPublicread.class);
    }

    public static boolean isPrivate(HasModifier e) {
        return containsType(e.getModifiers(), VisibilityPrivate.class);
    }

    public static boolean isStatic(HasModifier e) {
        return containsType(e.getModifiers(), ModStatic.class);
    }

    public static boolean isOverride(HasModifier e) {
        return containsType(e.getModifiers(), ModOverride.class);
    }

    public static boolean isAbstract(HasModifier e) {
        if (e instanceof FuncDef
                && e.attrNearestStructureDef() instanceof InterfaceDef) {
            FuncDef f = (FuncDef) e;
            // functions in interfaces are always abstract if they have no implementation ...
            return f.attrHasEmptyBody();
        }
        return containsType(e.getModifiers(), ModAbstract.class);
    }

    public static boolean isConstant(HasModifier e) {
        return containsType(e.getModifiers(), ModConstant.class);
    }

    static boolean containsType(de.peeeq.wurstscript.ast.Modifiers modifiers, Class<? extends Modifier> class1) {
        for (Modifier m : modifiers) {
            if (m.getClass().getName().startsWith(class1.getName())) {
                return true;
            }
        }
        return false;
    }


    public static boolean isCompiletime(HasModifier e) {
        if (e instanceof HasModifier) {
            return hasAnnotation((HasModifier) e, "compiletime");
        }
        return false;
    }

    public static boolean hasAnnotation(HasModifier e, String name) {
        return hasAnnotation(e.getModifiers(), name);
    }


    private static boolean hasAnnotation(Modifiers modifiers, String string) {
        for (Modifier m : modifiers) {
            if (m instanceof Annotation) {
                Annotation annotation = (Annotation) m;
                if (annotation.getAnnotationType().equals("@" + string)) {
                    return true;
                }
            }
        }
        return false;
    }


}
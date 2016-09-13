package odruba;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.PrintUtil;

import java.io.PrintStream;

public class OutputTools {

    public static void printModel(Model model) {
        printModel(model, System.out);
    }

    public static void printModel(Model model, PrintStream out) {
        StmtIterator iter = model.listStatements();
        while(iter.hasNext()) {
            Statement statement = iter.next();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();
            RDFNode  object = statement.getObject();

            out.print(subject.toString() + " ");
            out.print(predicate.toString() + " ");

            if (object instanceof Resource) {
                out.print(object.toString());
            } else {
                out.print(" \"" + object.toString() + "\"");
            }

            out.println(" .");
        }
    }

    public static void printStatements(Model m, Resource s, Property p, Resource o) {
        for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
            Statement stmt = i.nextStatement();
            System.out.println(" - " + PrintUtil.print(stmt));
        }
    }

    public static void separator() {
        separator(40, 0, null);
    }

    public static void separator(String s) {
        separator(40, 10, s);
    }

    public static void separator(int i, int offset, String s){
        if(s != null && !s.equals("")) {
            int endLength = i - s.length() - 2 - offset;
            String dashesBegin = new String(new char[offset]).replace('\0', '-');
            String dashesEnd = new String(new char[endLength]).replace('\0', '-');
            System.out.println(dashesBegin + "( " + s + " )" + dashesEnd);
        }
        else {
            String dashes = new String(new char[i]).replace('\0', '-');
            System.out.println(dashes);
        }
    }
}

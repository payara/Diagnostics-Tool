package fish.payara.extras.diagnostics.util;

import java.io.BufferedReader;
import java.io.StringReader;

public class StringBuilderNewLineAppender {

    private  StringBuilder sb;
    static final String SEP = System.getProperty("line.separator");
    /** Creates a new instance of StringBuilderNewLineAppender */
    public StringBuilderNewLineAppender(final StringBuilder sb) {
        this.sb = sb;
    }
    public StringBuilderNewLineAppender append(final String s) {
        sb.append(s);
        sb.append(SEP);
        return ( this );
    }
    public String toString() {
        return ( sb.toString() );
    }
    public String toString(String... filterOut) {
        String sbString = sb.toString();
        BufferedReader in = new BufferedReader(new StringReader(sbString));
        sb = new StringBuilder();

        try
        {
            readloop:
            for(String s = in.readLine(); s != null; s = in.readLine()){
                for(String filter : filterOut){
                    if(s.startsWith(filter))
                        continue readloop; // continue to outer loop
                }
                append(s);
            }
        }
        catch(Exception e)
        {
            // bail
            return sbString;
        }

        return toString();
    }

}

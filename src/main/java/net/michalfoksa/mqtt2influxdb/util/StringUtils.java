package net.michalfoksa.mqtt2influxdb.util;

public class StringUtils {

    /***
     * Converts wildcard string to regular expression. Characters with special
     * meaning in wildcard string are:
     * <li> star (*) - any number of any characters
     * <li> question mark (?) - exactly one character
     *
     * @param wildcard
     * @return regular expression
     */
    public static String wildcardToRegex(String wildcard){
        boolean esc = false;
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch(c) {

                // * becomes .* unless it is escaped
                case '*':
                    if (esc) {
                        s.append('*');
                        esc = false;
                    } else {
                        s.append(".*");
                    }
                    break;

                 // ? becomes . unless it is escaped
                case '?':
                    if (esc) {
                        s.append('?');
                        esc = false;
                    } else {
                        s.append('.');
                    }
                    break;

                case '\\':
                    s.append("\\");
                    esc = !esc;
                    break;

                // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '+': case '@': case '%':
                    if(esc){
                        s.append("\\");
                        esc = false;
                    }
                    s.append("\\");
                    s.append(c);
                    break;

                default:
                    if(esc){
                        s.append("\\");
                        esc = false;
                    }
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return(s.toString());
    }

}

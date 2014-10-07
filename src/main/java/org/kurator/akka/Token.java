package org.kurator.akka;

/* 
 * NOTE: This code was derived from akka.Token in the FilteredPush repository at
 * svn://svn.code.sf.net/p/filteredpush/svn/trunk/FP-Akka as of 07Oct2014. 
 */

public class Token<T> {
    private final T data;

    public Token(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}

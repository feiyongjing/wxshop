package com.feiyongjing.wxshop;

import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class Main {
    public static Object obj = new Object();
    public String name;
    public Main main;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Main main1 = (Main) o;
        return Objects.equals(name, main1.name) &&
                Objects.equals(main, main1.main);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, main);
    }

    public static void main(String[] args) {
        int[] a=new int[]{3,6,2,5,7,83,4,5,6};
        Arrays.sort(a);
        System.out.println(freqAlphabets("10#11#12"));
    }

    public static String freqAlphabets(String s) {
        StringBuilder res=new StringBuilder();
        int a=0;
        while(a<s.length()){
            if(a+2<s.length() && s.charAt(a+2)=='#'){
                res.append((char)(Integer.valueOf(s.substring(a,a+2))-1+'a'));
                a+=3;
            }else{
                res.append((char)(1+'a'));
                a++;
            }
        }
        return res.toString();
    }

}

class GC {

    public static GC SAVE_HOOK = null;
    public String gcString;

    public GC(String gcString) {
        this.gcString = gcString;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("execute method finalize()");
        SAVE_HOOK = this;
    }
}

package com.ngc.seaside.service;

import com.ngc.seaside.service.helloworld.Hello;

public class Main {
   public static void main(String[] args) {
      Hello hello = new Hello();
      System.out.println(hello.sayHello());
   }
}


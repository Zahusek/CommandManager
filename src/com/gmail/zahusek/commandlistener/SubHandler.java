package com.gmail.zahusek.commandlistener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubHandler {

	String parent();

	String[] args();

	String permission() default "";

	String permissionMessage() default "&cYou do not have permission to use that subcommand";

}

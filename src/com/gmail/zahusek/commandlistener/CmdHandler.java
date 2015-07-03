package com.gmail.zahusek.commandlistener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CmdHandler {

	String name();

	String[] aliases() default { "" };

	String description() default "";

	String permission() default "";

	String permissionMessage() default "&cYou do not have permission to use that command";

}

/*
---------------------------------------------------------------------------------
File Name : Validation

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 10/07/2024
Last Modified : 10/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.bucket;

import lombok.ToString;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Validation annotation is used to define validation rules for fields.
 * It can be applied to fields only.
 *
 * Usage example:
 *
 * ```
 * @Validation(type = ValidationType.STRING, required = true, minLength = 2, maxLength = 4)
 * private String language;
 * ```
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Validation {
    ValidationType type() default ValidationType.OBJECT;
    int maxLength() default Integer.MAX_VALUE;
    int minLength() default 0;
    boolean readOnly() default false;
    boolean required() default false;
    String regex() default "";

    Class<?> instancedOf() default Class.class;
}
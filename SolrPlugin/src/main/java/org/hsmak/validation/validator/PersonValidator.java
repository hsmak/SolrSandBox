package org.hsmak.validation.validator;

import org.hsmak.validation.model.Person;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Created by hsmak on 9/3/16.
 */
public class PersonValidator implements Validator {

    private AddressValidator addressValidator;

    public boolean supports(Class<?> clazz) {
        return Person.class.equals(clazz);
    }

    public void validate(Object o, Errors errors) {

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "name.required");

        Person person = (Person) o;

        if (person.getAge() < 0) {
            errors.rejectValue("age", "negativevalue");
        } else if (person.getAge() > 110) {
            errors.rejectValue("age", "too.darn.old");
        }

        try {

            errors.pushNestedPath("address");
            ValidationUtils.invokeValidator(this.addressValidator, person.getAddress(), errors);
        }
        finally {
            errors.popNestedPath();
        }
    }

    public AddressValidator getAddressValidator() {
        return addressValidator;
    }

    @Required
    public void setAddressValidator(AddressValidator addressValidator) {
        this.addressValidator = addressValidator;
    }
}

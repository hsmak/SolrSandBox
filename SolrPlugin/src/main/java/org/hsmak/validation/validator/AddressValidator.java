package org.hsmak.validation.validator;

import org.hsmak.validation.model.Address;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Created by hsmak on 9/3/16.
 */
public class AddressValidator implements Validator {

    public boolean supports(Class<?> clazz) {
        return Address.class.equals(clazz);
    }

    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "streetNumber", "streetNumber.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "StreetName", "StreetName.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "city", "city.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "Zipcode", "Zipcode.empty");
    }
}

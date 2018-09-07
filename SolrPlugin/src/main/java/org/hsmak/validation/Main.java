package org.hsmak.validation;

import org.hsmak.validation.model.Address;
import org.hsmak.validation.model.Person;
import org.hsmak.validation.validator.PersonValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

/**
 * Created by hsmak on 9/3/16.
 */
public class Main {

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        PersonValidator personValidator = (PersonValidator)context.getBean("personValidator");

        Address address = new Address();
        Person person = new Person();

        DataBinder dataBinder = new DataBinder(person);
        dataBinder.setValidator(personValidator);
        dataBinder.validate();

        BindingResult bindingResult = dataBinder.getBindingResult();
        System.out.println(bindingResult);
    }

}

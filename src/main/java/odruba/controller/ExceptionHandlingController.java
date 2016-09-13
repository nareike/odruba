package odruba.controller;

import org.apache.jena.riot.RiotException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
public class ExceptionHandlingController {

    @ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Error parsing Turtle input.")
    @ExceptionHandler(RiotException.class)
    public void RiotError() {
        // nothing to do
    }

}

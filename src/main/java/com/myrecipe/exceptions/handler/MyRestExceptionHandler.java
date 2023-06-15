package com.myrecipe.exceptions.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.myrecipe.exceptions.DuplicateRecordFoundException;
import com.myrecipe.exceptions.ImageFormatException;
import com.myrecipe.exceptions.InvalidCategoryException;
import com.myrecipe.exceptions.InvalidLoginDataException;
import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.entities.responses.MyApiErrorResponse;

@ControllerAdvice
public class MyRestExceptionHandler {

//    @ExceptionHandler(RecordNotFoundException.class)
//    public ResponseEntity<MyApiErrorResponse> handleRecordNotFoundException (RecordNotFoundException e) {
//        MyApiErrorResponse response = new MyApiErrorResponse(
//                "Record with the specified value was not found.",
//                e.getMessage() + " Check the entered data and try again.",
//                HttpStatus.NOT_FOUND);
//
//        return new ResponseEntity<> (response, HttpStatus.NOT_FOUND);
//    }

    @ExceptionHandler(RecordNotFoundException.class)
    public String handleRecordNotFoundExceptionAsString(RecordNotFoundException e, Model model) {
        model.addAttribute("error", "Опа! Възникна грешка!");
        MyApiErrorResponse response = new MyApiErrorResponse(
                "Не е намерен запис по посочените данни",
                e.getMessage() + " Проверете въведените данни и опитайте отново",
                HttpStatus.NOT_FOUND);
        model.addAttribute("response", response);
        return "error";
    }

//    @ExceptionHandler(InvalidUserRequestException.class)
//    public ResponseEntity<MyApiErrorResponse> handleInvalidUserRequestException (InvalidUserRequestException e) {
//        MyApiErrorResponse response = new MyApiErrorResponse(
//                e.getMessage(),
//                "Your request is invalid. Please make sure you entered the correct data and try again.",
//                HttpStatus.BAD_REQUEST);
//
//        return new ResponseEntity<> (response, HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(InvalidUserRequestException.class)
    public String handleInvalidUserRequestExceptionAsString(InvalidUserRequestException e, Model model) {
        model.addAttribute("error", "Опа! Възникна грешка!");
        MyApiErrorResponse response = new MyApiErrorResponse(
                e.getMessage(),
                "Заявката ви е невалидна. Моля проверете дали сте въвели правилната информация и опитайте отново.",
                HttpStatus.BAD_REQUEST);
        model.addAttribute("response", response);
        return "error";
    }

//    @ExceptionHandler(SecurityException.class)
//    public ResponseEntity<MyApiErrorResponse> handleSecurityException (SecurityException e) {
//        MyApiErrorResponse response = new MyApiErrorResponse(
//                "Wrong email or password for accessing private recipes submitted.",
//                e.getMessage(),
//                HttpStatus.FORBIDDEN);
//
//        return new ResponseEntity<> (response, HttpStatus.FORBIDDEN);
//    }

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityExceptionAsString(SecurityException e, Model model) {
        model.addAttribute("error", "Опа! Възникна грешка!");
        MyApiErrorResponse response = new MyApiErrorResponse(
                e.getMessage(),
                "Подадена е грешна парола при опит за достъп до тайни рецепти.",
                HttpStatus.FORBIDDEN);
        model.addAttribute("response", response);
        return "error";
    }

//    @ExceptionHandler(InvalidLoginDataException.class)
//    public ResponseEntity<MyApiErrorResponse> handleInvalidLoginDataException (InvalidLoginDataException e) {
//        MyApiErrorResponse response = new MyApiErrorResponse(
//                e.getMessage(),
//                "The email or password you provided are either wrong or invalid. Please try again.",
//                HttpStatus.FORBIDDEN);
//
//        return new ResponseEntity<> (response, HttpStatus.FORBIDDEN);
//    }

    @ExceptionHandler(InvalidLoginDataException.class)
    public String handleInvalidLoginDataExceptionAsString(InvalidLoginDataException e, Model model) {
        model.addAttribute("error", "Опа! Възникна грешка!");
        MyApiErrorResponse response = new MyApiErrorResponse(
                e.getMessage(),
                "Имейлът или паролата, които подадохте, са неправилни.",
                HttpStatus.FORBIDDEN);
        model.addAttribute("response", response);
        return "error";
    }

//    @ExceptionHandler(DuplicateRecordFoundException.class)
//    public ResponseEntity<MyApiErrorResponse> handleDuplicateRecordFoundException (DuplicateRecordFoundException e) {
//        MyApiErrorResponse response = new MyApiErrorResponse(
//                "The value provided is duplicate. Please enter another value!",
//                e.getMessage(),
//                HttpStatus.CONFLICT);
//
//        return new ResponseEntity<> (response, HttpStatus.CONFLICT);
//    }

    @ExceptionHandler(DuplicateRecordFoundException.class)
    public String handleDuplicateRecordFoundExceptionAsString(DuplicateRecordFoundException e, Model model) {
        model.addAttribute("error", "Опа! Възникна грешка!");
        MyApiErrorResponse response = new MyApiErrorResponse(
                "Опит за запис на дублиращи се данни!",
                e.getMessage(),
                HttpStatus.CONFLICT);
        model.addAttribute("response", response);
        return "error";
    }

//    @ExceptionHandler(InvalidCategoryException.class)
//    public ResponseEntity<MyApiErrorResponse> handleInvalidCategoryException (InvalidCategoryException e) {
//        MyApiErrorResponse response = new MyApiErrorResponse(
//                e.getMessage(),
//                "Please check the category field and try again!",
//                HttpStatus.BAD_REQUEST);
//
//        return new ResponseEntity<> (response, HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(InvalidCategoryException.class)
    public String handleInvalidCategoryExceptionAsString(InvalidCategoryException e, Model model) {
        model.addAttribute("error", "Опа! Възникна грешка!");
        MyApiErrorResponse response = new MyApiErrorResponse(
                e.getMessage(),
                "Проверете категорията, която сте въвели и опитайте отново!",
                HttpStatus.BAD_REQUEST);
        model.addAttribute("response", response);
        return "error";
    }

//    @ExceptionHandler(ImageFormatException.class)
//    public ResponseEntity<MyApiErrorResponse> handleImageFormatException (ImageFormatException e) {
//        MyApiErrorResponse response = new MyApiErrorResponse(
//                e.getMessage(),
//                "Please ensure that you uploaded a valid image!",
//                HttpStatus.UNSUPPORTED_MEDIA_TYPE);
//
//        return new ResponseEntity<> (response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
//    }

    @ExceptionHandler(ImageFormatException.class)
    public String handleImageFormatExceptionAsString(ImageFormatException e, Model model) {
        model.addAttribute("error", "Опа! Възникна грешка!");
        MyApiErrorResponse response = new MyApiErrorResponse(
                e.getMessage(),
                "Уверете се, че качвате валидно изображение!",
                HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        model.addAttribute("response", response);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleExceptionAsString(Exception e, Model model) {
        model.addAttribute("error", "Опа! Нещо се обърка!");
        MyApiErrorResponse response = new MyApiErrorResponse(
                "Неочаквана грешка!",
                HttpStatus.BAD_REQUEST);
        System.out.println(e.getMessage());
        model.addAttribute("response", response);
        return "error";
    }
}

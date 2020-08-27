package com.github.davidmoten.msgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Group {

    private final String name;
    private final List<Person> persons;

    public Group(String name, List<Person> persons) {
        this.name = name;
        this.persons = persons;
    }

    public String name() {
        return name;
    }

    public List<Person> persons() {
        return persons;
    }

    public static Builder name(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        final String name;
        List<Person> persons = new ArrayList<>();

        Builder(String name) {
            this.name = name;
        }

        Builder2 firstName(String firstName) {
            return new Builder2(this, firstName);
        }

        public Group build() {
            return new Group(name, persons);
        }
    }

    public static final class Builder2 {

        private final Builder b;
        private final String firstName;
        private String lastName;
        private Optional<Integer> yearOfBirth = Optional.empty();

        Builder2(Builder b, String firstName) {
            this.b = b;
            this.firstName = firstName;
        }

        public Builder3 lastName(String lastName) {
            this.lastName = lastName;
            return new Builder3(this);
        }
    }

    public static final class Builder3 {

        private final Builder2 person;

        Builder3(Builder2 person) {
            this.person = person;
        }

        public Builder3 yearOfBirth(int yearOfBirth) {
            person.yearOfBirth = Optional.of(yearOfBirth);
            return this;
        }

        public Builder2 firstName(String firstName) {
            Person p = new Person(person.firstName, person.lastName, person.yearOfBirth);
            person.b.persons.add(p);
            return person.b.firstName(firstName);
        }

        public Group build() {
            return person.b.build();
        }
    }

    public static final class Person {
        private final String firstName;
        private final String lastName;

        private final Optional<Integer> yearOfBirth;

        public Person(String firstName, String lastName, Optional<Integer> yearOfBirth) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.yearOfBirth = yearOfBirth;
        }

        public String firstName() {
            return firstName;
        }

        public String lastName() {
            return lastName;
        }

        public Optional<Integer> yearOfBirth() {
            return yearOfBirth;
        }
    }

    public static void main(String[] args) {
        Group //
                .name("friends") //
                .firstName("John") //
                .lastName("Smith") //
                .yearOfBirth(1965) //
                .firstName("Anne") //
                .lastName("Jones") //
                .build();
    }

}

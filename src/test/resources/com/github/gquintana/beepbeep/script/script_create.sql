CREATE TABLE person(
  login VARCHAR (64) PRIMARY KEY ,
  email VARCHAR (256) NOT NULL
);

CREATE INDEX person_email ON person(email);
